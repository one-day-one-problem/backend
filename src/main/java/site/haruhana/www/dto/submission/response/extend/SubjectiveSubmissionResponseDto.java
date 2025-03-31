package site.haruhana.www.dto.submission.response.extend;

import lombok.Getter;
import site.haruhana.www.dto.submission.response.SubmissionResponseDto;
import site.haruhana.www.entity.submission.subjective.SubjectiveSubmission;
import site.haruhana.www.entity.submission.Submission;

import java.time.LocalDateTime;

@Getter
public class SubjectiveSubmissionResponseDto extends SubmissionResponseDto {
    private final Double score;
    private final String feedback;
    private final LocalDateTime feedbackProvidedAt;
    private final Boolean isPending;

    public SubjectiveSubmissionResponseDto(Submission submission) {
        super(submission.getId(), submission.getSubmittedAt());

        if (submission instanceof SubjectiveSubmission subjectiveSubmission) { // 주관식 제출인 경우
            this.score = subjectiveSubmission.getScore();
            this.feedback = subjectiveSubmission.getFeedback();
            this.feedbackProvidedAt = subjectiveSubmission.getFeedbackProvidedAt();
            this.isPending = subjectiveSubmission.getFeedbackProvidedAt() == null;

        } else { // 객관식 제출인 경우
            this.score = null;
            this.feedback = null;
            this.feedbackProvidedAt = null;
            this.isPending = true;
        }
    }
}
