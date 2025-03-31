package site.haruhana.www.entity.submission.subjective;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import site.haruhana.www.entity.submission.Submission;
import site.haruhana.www.service.AIService.GradingResult;

import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@DiscriminatorValue("SUBJECTIVE")
public class SubjectiveSubmission extends Submission {

    /**
     * 사용자가 제출한 답안에 대한 점수
     */
    private Double score;

    /**
     * 사용자가 제출한 답안에 대한 피드백 (by AI)
     */
    @Column(columnDefinition = "TEXT")
    private String feedback;

    /**
     * 피드백이 제공된 시각
     */
    private LocalDateTime feedbackProvidedAt;

    /**
     * 주관식 문제 답안의 채점 결과를 업데이트하는 메서드
     *
     * @param result 채점 결과
     */
    public void updateGradingResult(GradingResult result) {
        super.setIsCorrect(result.isCorrect());
        this.score = result.score();
        this.feedback = result.feedback();
        this.feedbackProvidedAt = LocalDateTime.now();
    }
}
