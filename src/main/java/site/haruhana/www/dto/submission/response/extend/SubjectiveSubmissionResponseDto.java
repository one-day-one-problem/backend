package site.haruhana.www.dto.submission.response.extend;

import lombok.Getter;
import site.haruhana.www.dto.submission.response.SubmissionResponseDto;
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
        this.score = submission.getScore();
        this.feedback = submission.getFeedback();
        this.feedbackProvidedAt = submission.getFeedbackProvidedAt();
        this.isPending = submission.getFeedback() == null;
    }
}
