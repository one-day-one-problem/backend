package site.haruhana.www.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.haruhana.www.dto.submission.request.SubmissionRequestDto;
import site.haruhana.www.dto.submission.response.SubmissionResponseDto;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.problem.ProblemType;
import site.haruhana.www.entity.problem.choice.ProblemOption;
import site.haruhana.www.entity.submission.Submission;
import site.haruhana.www.entity.submission.choice.MultipleChoiceSubmission;
import site.haruhana.www.entity.submission.subjective.SubjectiveSubmission;
import site.haruhana.www.entity.user.User;
import site.haruhana.www.exception.InvalidAnswerFormatException;
import site.haruhana.www.exception.ProblemNotFoundException;
import site.haruhana.www.queue.SubmissionMessageQueue;
import site.haruhana.www.queue.message.GradingData;
import site.haruhana.www.repository.ProblemRepository;
import site.haruhana.www.repository.SubmissionRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionMessageQueue messageQueue;

    private final ProblemRepository problemRepository;

    private final SubmissionRepository submissionRepository;

    /**
     * 사용자 답안 제출 처리
     *
     * @param user       현재 로그인된 사용자
     * @param problemId  문제 ID
     * @param requestDto 제출 요청 DTO
     * @return 제출 결과 응답 DTO
     */
    @Transactional
    public SubmissionResponseDto submitAnswer(User user, Long problemId, SubmissionRequestDto requestDto) {
        // 문제 조회
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(ProblemNotFoundException::new);

        // 답안 제출 객체 생성
        Submission submission = Submission.from(user, problem, requestDto.getAnswer(), requestDto.getDuration());

        // 제출 저장 (ID 부여를 위해 먼저 저장)
        submission = submissionRepository.save(submission);

        // 문제 유형에 따라 처리
        if (problem.getType() == ProblemType.MULTIPLE_CHOICE) { // 객관식 문제인 경우
            MultipleChoiceSubmission multipleChoiceSubmission = (MultipleChoiceSubmission) submission;
            boolean isCorrect = gradeMultipleChoiceSubmission(multipleChoiceSubmission); // 문제를 채점하고
            multipleChoiceSubmission.updateGradingResult(isCorrect); // 정답 여부 업데이트

            if (isCorrect) { // 사용자가 정답을 맞췄다면
                problem.incrementSolvedCount(); // 문제 풀이 횟수 증가
            }

        } else { // 주관식 문제인 경우
            SubjectiveSubmission subjectiveSubmission = (SubjectiveSubmission) submission;
            messageQueue.enqueue(GradingData.fromSubmission(subjectiveSubmission));
        }

        // 응답 생성 및 반환
        return SubmissionResponseDto.fromSubmission(submission);
    }

    /**
     * 객관식 문제를 채점하는 메서드
     *
     * @param submission 채점할 제출 내역
     * @return 채점 결과 (true: 정답, false: 오답)
     */
    private boolean gradeMultipleChoiceSubmission(MultipleChoiceSubmission submission) {
        // 제출 정보에서 필요한 데이터 가져오기
        Problem problem = submission.getProblem();
        String submittedAnswer = submission.getSubmittedAnswer();

        // 제출된 답변을 옵션 ID 집합으로 변환
        Set<Long> submittedOptionIds = parseSubmittedAnswer(submittedAnswer);

        // 문제의 정답 옵션 정보 가져오기
        Set<Long> correctOptionIds = getCorrectOptionIds(problem);

        // 정답 여부 판단하여 반환
        return isAnswerCorrect(submittedOptionIds, correctOptionIds);
    }

    /**
     * 제출된 답안을 옵션 ID 집합으로 변환하는 메서드
     *
     * @param submittedAnswer 제출된 답안 (쉼표로 구분된 문자열)
     * @return 옵션 ID 집합
     * @throws InvalidAnswerFormatException 입력값이 숫자가 아닌 경우
     */
    private Set<Long> parseSubmittedAnswer(String submittedAnswer) {
        if (submittedAnswer == null || submittedAnswer.trim().isEmpty()) {
            return Collections.emptySet();
        }

        try {
            return Arrays.stream(submittedAnswer.split(","))
                    .map(String::trim)
                    .map(Long::valueOf)
                    .collect(Collectors.toSet());

        } catch (NumberFormatException e) {
            throw new InvalidAnswerFormatException();
        }
    }

    /**
     * 문제의 정답 옵션 ID 집합을 가져오는 메서드
     *
     * @param problem 문제 엔티티
     * @return 정답 옵션 ID 집합
     */
    private Set<Long> getCorrectOptionIds(Problem problem) {
        return problem.getProblemOptions().stream()
                .filter(ProblemOption::isCorrect)
                .map(ProblemOption::getId)
                .collect(Collectors.toSet());
    }

    /**
     * 제출된 답변과 정답을 비교하여 정답 여부를 판단하는 메서드
     *
     * @param submittedOptionIds 제출된 답안 옵션 ID 집합
     * @param correctOptionIds   정답 옵션 ID 집합
     */
    private boolean isAnswerCorrect(Set<Long> submittedOptionIds, Set<Long> correctOptionIds) {
        if (submittedOptionIds.size() != correctOptionIds.size()) {
            return false;
        }

        return submittedOptionIds.containsAll(correctOptionIds);
    }

}

