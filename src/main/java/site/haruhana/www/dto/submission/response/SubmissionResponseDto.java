package site.haruhana.www.dto.submission.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import site.haruhana.www.entity.submission.Submission;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmissionResponseDto {
    
    private Long id;
    private LocalDateTime submittedAt;

    // 객관식 문제 전용 필드
    private Boolean isCorrect;
    
    // 주관식 문제 전용 필드
    private Double score;
    private String feedback;
    private LocalDateTime feedbackProvidedAt;
    private Boolean isPending;  // 채점 대기중 여부
    
    /**
     * 객관식 문제 제출에 대한 응답 DTO 생성하는 정적 팩토리 메서드
     */
    public static SubmissionResponseDto fromMultipleChoice(Submission submission) {
        return SubmissionResponseDto.builder()
                .id(submission.getId())
                .submittedAt(submission.getSubmittedAt())
                .isCorrect(submission.getIsCorrect())
                .build();
    }
    
    /**
     * 주관식 문제 제출에 대한 응답 DTO 생성하는 정적 팩토리 메서드
     */
    public static SubmissionResponseDto fromSubjective(Submission submission) {
        boolean isPending = submission.getFeedback() == null;
        
        return SubmissionResponseDto.builder()
                .id(submission.getId())
                .submittedAt(submission.getSubmittedAt())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .feedbackProvidedAt(submission.getFeedbackProvidedAt())
                .isPending(isPending)
                .build();
    }

}
