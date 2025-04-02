package site.haruhana.www.dto.submission.response.extend;

import lombok.Getter;
import site.haruhana.www.dto.submission.response.SubmissionResponseDto;
import site.haruhana.www.entity.submission.Submission;

@Getter
public class MultipleChoiceSubmissionResponseDto extends SubmissionResponseDto {
    private final Boolean isCorrect;

    public MultipleChoiceSubmissionResponseDto(Submission submission) {
        super(submission.getId(), submission.getSubmittedAt());
        this.isCorrect = submission.getIsCorrect();
    }
}
