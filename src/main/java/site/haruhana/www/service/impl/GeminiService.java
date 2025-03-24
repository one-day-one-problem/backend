package site.haruhana.www.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.ProblemProvider;
import site.haruhana.www.feign.GeminiFeignClient;
import site.haruhana.www.feign.dto.gemini.GeminiRequest;
import site.haruhana.www.queue.message.GradingData;
import site.haruhana.www.service.AIService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Google Gemini AI를 사용하여 문제 생성 및 채점을 수행하는 서비스 클래스
 *
 * @see <a href="https://ai.google.dev/gemini-api/docs?hl=ko">Gemini API 문서</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService implements AIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final GeminiFeignClient geminiFeignClient;

    private final ObjectMapper objectMapper;

    @Override
    public Problem generateMultipleChoiceQuestion(ProblemCategory category, ProblemDifficulty difficulty) {
        try {
            String prompt = String.format(MULTIPLE_CHOICE_PROMPT, category.getDescription(), difficulty.name());
            JsonNode json = getAIGeneratedContent(prompt);

            Problem problem = Problem.multipleChoiceProblemBuilder()
                    .title(json.get("title").asText())
                    .question(json.get("question").asText())
                    .category(category)
                    .difficulty(difficulty)
                    .provider(ProblemProvider.AI)
                    .build();

            // 문제의 보기 추가
            JsonNode options = json.get("options");
            if (options != null && options.isArray()) {
                for (JsonNode option : options) {
                    problem.addOption(option.get("content").asText(), option.get("isCorrect").asBoolean());
                }
            }

            return problem;

        } catch (Exception e) {
            log.error("객관식 문제 생성 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("AI를 통한 객관식 문제 생성에 실패했습니다", e);
        }
    }

    @Override
    public Problem generateSubjectiveQuestion(ProblemCategory category, ProblemDifficulty difficulty) {
        try {
            String prompt = String.format(SUBJECTIVE_PROMPT, category.getDescription(), difficulty.name());
            JsonNode json = getAIGeneratedContent(prompt);

            Problem problem = Problem.subjectiveProblemBuilder()
                    .title(json.get("title").asText())
                    .question(json.get("question").asText())
                    .expectedAnswerLength(json.get("expectedLength").asText())
                    .category(category)
                    .difficulty(difficulty)
                    .provider(ProblemProvider.AI)
                    .sampleAnswer(json.get("sampleAnswer").asText())
                    .build();

            // 문제의 평가 요소 추가
            JsonNode evaluationPoints = json.get("evaluationPoints");
            if (evaluationPoints != null && evaluationPoints.isArray()) {
                for (JsonNode point : evaluationPoints) {
                    problem.addGradingCriteria(point.asText());
                }
            }

            return problem;

        } catch (Exception e) {
            log.error("주관식 문제 생성 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("AI를 통한 주관식 문제 생성에 실패했습니다", e);
        }
    }

    @Override
    public GradingResult gradeSubjectiveSubmission(GradingData data) {
        try {
            // 채점 기준 목록을 번호가 있는 목록 형태로 변환
            String formattedCriteria = IntStream.range(0, data.getGradingCriteria().size())
                    .mapToObj(i -> (i + 1) + ". " + data.getGradingCriteria().get(i))
                    .collect(Collectors.joining("\n"));

            // 프롬프트 생성
            String gradingPrompt = String.format(
                    SUBJECTIVE_GRADING_PROMPT,
                    data.getProblemTitle(),
                    data.getProblemQuestion(),
                    formattedCriteria,
                    data.getSampleAnswer(),
                    data.getSubmittedAnswer()
            );

            // AI에 채점 요청
            JsonNode json = getAIGeneratedContent(gradingPrompt);

            // 피드백 가져오기
            String overallFeedback = json.get("feedback").asText();
            JsonNode criteriaEvaluations = json.get("criteriaEvaluation");

            // 피드백을 정리하기 위한 빌더
            StringBuilder feedbackBuilder = new StringBuilder();

            // 평균 점수 계산을 위한 변수
            double totalScore = 0;
            int criteriaCount = 0;

            if (criteriaEvaluations != null && criteriaEvaluations.isArray()) {
                for (int i = 0; i < criteriaEvaluations.size(); i++) {
                    // i번째 채점 기준 평가 정보
                    JsonNode evaluation = criteriaEvaluations.get(i);

                    // 평가 정보 파싱
                    String criteriaName = evaluation.get("criteria").asText();
                    int criteriaScore = evaluation.get("score").asInt();
                    String criteriaFeedback = evaluation.get("feedback").asText();

                    // 평균 점수 계산을 위해 누적
                    totalScore += criteriaScore;
                    criteriaCount++;

                    // 피드백 빌더에 추가
                    feedbackBuilder.append("## ").append(i + 1).append(". ")
                            .append(criteriaName)
                            .append(" (").append(criteriaScore).append("점)\n\n")
                            .append(criteriaFeedback).append("\n\n");
                }
            }

            // 평균 점수 계산 (소수점 둘째 자리에서 반올림)
            double averageScore = 0;
            if (criteriaCount > 0) {
                averageScore = new BigDecimal(totalScore / criteriaCount)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            // 종합 평가 추가
            feedbackBuilder.insert(0, "# 종합 평가 결과 (" + averageScore + "점)\n\n" + overallFeedback + "\n\n");

            // 주관식 문제 해결 여부 판단
            boolean isCorrect = averageScore >= PASSING_SCORE;

            log.info("제출 #{} 채점 완료: {}점 (정답 여부: {})", data.getSubmissionId(), averageScore, isCorrect);

            // 결과 반환
            return new GradingResult(averageScore, isCorrect, feedbackBuilder.toString().trim());

        } catch (Exception e) {
            log.error("제출 #{} 채점 중 오류 발생: {}", data.getSubmissionId(), e.getMessage());
            throw new RuntimeException("AI를 통한 주관식 문제 채점에 실패했습니다", e);
        }
    }

    private JsonNode getAIGeneratedContent(String prompt) {
        try {
            String response = geminiFeignClient.generateContent(apiKey, GeminiRequest.of(prompt))
                    .getCandidates().get(0)
                    .getContent()
                    .getParts().get(0)
                    .getText();

            // 백틱으로 감싸진 JSON 응답 처리
            if (response.startsWith("```json")) {
                response = response.substring(7, response.length() - 3).trim();
            } else if (response.startsWith("```")) {
                response = response.substring(3, response.length() - 3).trim();
            }

            return objectMapper.readTree(response);

        } catch (FeignException e) {
            log.error("Gemini API 호출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("Gemini API 호출에 실패했습니다", e);

        } catch (JsonProcessingException e) {
            log.error("AI 응답 처리 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("AI 응답을 처리할 수 없습니다", e);
        }
    }
}
