package site.haruhana.www.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import site.haruhana.www.entity.submission.Submission;
import site.haruhana.www.queue.SubmissionMessageQueue;
import site.haruhana.www.queue.wrapper.GradingData;
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
            GradingData gradingData = messageQueue.dequeue();

            if (gradingData != null) {
                gradeSubjectiveSubmission(gradingData);
            }
        }
    }

    /**
     * 주관식 문제를 채점하는 메서드
     *
     * @param gradingData 채점할 데이터
     */
    private void gradeSubjectiveSubmission(GradingData gradingData) {
        try {
            // AI 서비스를 통한 채점 요청
            GradingResult result = aiService.gradeSubjectiveSubmission(gradingData);

            // 제출물 조회
            Submission submission = submissionRepository.findById(gradingData.getSubmissionId())
                    .orElseThrow(() -> new IllegalArgumentException("제출 정보가 존재하지 않습니다"));

            // 채점 결과 업데이트
            submission.updateGradingResult(result.score(), result.feedback());
            submissionRepository.save(submission);

            log.info("주관식 문제 제출 #{} 채점 완료: {}점 / 남은 채점 대기 수: {}", gradingData.getSubmissionId(), result.score(), messageQueue.size());

        } catch (Exception e) { // AI 서비스 호출 중 오류 발생 시
            log.error("제출 #{} 채점 중 오류 발생: {}", gradingData.getSubmissionId(), e.getMessage());
            messageQueue.prioritize(gradingData); // 높은 우선순위로 다시 큐에 넣어 재시도
        }
    }

}
