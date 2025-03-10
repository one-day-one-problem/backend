package site.haruhana.www.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.problem.ProblemProvider;
import site.haruhana.www.feign.GeminiFeignClient;
import site.haruhana.www.feign.dto.gemini.GeminiRequest;
import site.haruhana.www.service.AIService;

/**
 * Google Gemini AI를 사용하여 프로그래밍 문제를 자동 생성하는 서비스
 *
 * @see <a href="https://ai.google.dev/gemini-api/docs?hl=ko">Gemini API 문서</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements AIService {

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
