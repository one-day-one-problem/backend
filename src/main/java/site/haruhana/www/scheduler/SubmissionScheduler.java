package site.haruhana.www.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.problem.ProblemType;
import site.haruhana.www.entity.submission.Submission;
import site.haruhana.www.queue.SubmissionMessageQueue;
import site.haruhana.www.repository.SubmissionRepository;
import site.haruhana.www.service.AIService;
import site.haruhana.www.service.AIService.GradingResult;

/**
 * 주관식 답안 채점을 위한 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionScheduler {

    private final SubmissionMessageQueue messageQueue;

    private final SubmissionRepository submissionRepository;

    private final AIService aiService;

    /**
     * 특정 시간 간격으로 주관식 답안 채점 처리하는 스케줄러
     */
    @Scheduled(fixedDelay = 6000) // 6초마다 실행
    public void processSubjectiveSubmission() throws InterruptedException {
        if (!messageQueue.isEmpty()) {
            Submission submission = messageQueue.dequeue();

            if (submission != null) {
                gradeSubjectiveSubmission(submission);
            }
        }
    }

    /**
     * 주관식 문제를 채점하는 메서드
     *
     * @param submission 채점할 제출 내역
     */
    @Transactional
    protected void gradeSubjectiveSubmission(Submission submission) {
        try {
            Problem problem = submission.getProblem();

            if (problem.getType() != ProblemType.SUBJECTIVE) {
                throw new IllegalArgumentException("주관식 문제만 채점할 수 있습니다.");
            }

            // AI 서비스를 통한 채점 요청
            GradingResult result = aiService.gradeSubjectiveSubmission(submission);

            // 채점 결과 저장
            submission.updateGradingResult(result.score(), result.feedback());
            submissionRepository.save(submission);

            log.info("주관식 문제 제출 #{} 채점 완료: {}점", submission.getId(), result.score());

        } catch (Exception e) {
            log.error("제출 #{} 채점 중 오류 발생: {}", submission.getId(), e.getMessage());
            messageQueue.prioritize(submission); // 오류 발생 시 큐의 맨 앞에 다시 추가
        }
    }

}
