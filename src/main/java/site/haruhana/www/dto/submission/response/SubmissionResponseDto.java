package site.haruhana.www.dto.submission.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import site.haruhana.www.dto.submission.response.extend.MultipleChoiceSubmissionResponseDto;
import site.haruhana.www.dto.submission.response.extend.SubjectiveSubmissionResponseDto;
import site.haruhana.www.entity.submission.Submission;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class SubmissionResponseDto {
    private final Long id;
    private final LocalDateTime submittedAt;

    protected SubmissionResponseDto(Long id, LocalDateTime submittedAt) {
        this.id = id;
        this.submittedAt = submittedAt;
    }

    public static SubmissionResponseDto fromSubmission(Submission submission) {
        return switch (submission.getProblem().getType()) {
            case MULTIPLE_CHOICE -> new MultipleChoiceSubmissionResponseDto(submission);
            case SUBJECTIVE -> new SubjectiveSubmissionResponseDto(submission);
        };
    }
}
