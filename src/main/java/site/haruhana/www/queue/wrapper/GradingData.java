package site.haruhana.www.queue.wrapper;

import lombok.Builder;
import lombok.Getter;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.submission.Submission;

import java.util.List;

/**
 * 주관식 답안 채점에 필요한 핵심 데이터만 포함하는 데이터 클래스
 */
@Getter
@Builder
public class GradingData {

    // 제출 ID
    private final Long submissionId;

    // 문제 ID
    private final Long problemId;

    // 문제 제목
    private final String problemTitle;

    // 문제 내용
    private final String problemQuestion;

    // 채점 기준
    private final List<String> gradingCriteria;

    // 예시 답안
    private final String sampleAnswer;

    // 사용자가 제출한 답변
    private final String submittedAnswer;

    /**
     * Submission 엔티티에서 GradingData 객체를 생성하는 팩토리 메서드
     *
     * @param submission 채점할 제출 정보
     * @return 채점에 필요한 데이터만 포함하는 GradingData 객체
     */
    public static GradingData fromSubmission(Submission submission) {
        Problem problem = submission.getProblem();

        return GradingData.builder()
                .submissionId(submission.getId())
                .problemId(problem.getId())
                .problemTitle(problem.getTitle())
                .problemQuestion(problem.getQuestion())
                .gradingCriteria(problem.getGradingCriteriaList())
                .sampleAnswer(problem.getSampleAnswer())
                .submittedAnswer(submission.getSubmittedAnswer())
                .build();
    }
}
