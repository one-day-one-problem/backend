package site.haruhana.www.entity.submission;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import site.haruhana.www.entity.user.User;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.service.AIService.GradingResult;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "submissions")
public class Submission {

    /**
     * 제출 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 문제를 푼 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    /**
     * 사용자가 푼 문제
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Problem problem;

    /**
     * 사용자가 답안을 제출한 시각
     */
    @CreatedDate
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    /**
     * 사용자가 문제 풀이에 소요된 시간(초)
     */
    @Column(nullable = false)
    private int duration;

    /**
     * 사용자가 제출한 답안
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String submittedAnswer; // 객관식인 경우에도 String으로 저장. 복수 정답인 경우 콤마(,)로 구분

    /**
     * 사용자가 제출한 답안의 정답 여부
     */
    @Column(name = "is_correct", nullable = true)
    private Boolean isCorrect;

    /**
     * (주관식) 사용자가 제출한 답안에 대한 점수
     */
    @Column(nullable = true)
    private Integer score;

    /**
     * (주관식) 사용자가 제출한 답안에 대한 피드백 (by AI)
     */
    @Column(columnDefinition = "TEXT", nullable = true)
    private String feedback;

    /**
     * (주관식) 피드백이 제공된 시각
     */
    @Column(nullable = true)
    private LocalDateTime feedbackProvidedAt;

    /**
     * 주관식 문제 답안의 채점 결과를 업데이트하는 메서드
     *
     * @param result 채점 결과
     */
    public void updateSubjectiveGradingResult(GradingResult result) {
        this.score = result.score();
        this.isCorrect = result.isCorrect();
        this.feedback = result.feedback();
        this.feedbackProvidedAt = LocalDateTime.now();
    }

    /**
     * 객관식 문제의 정답 여부를 설정하는 메서드
     *
     * @param isCorrect 정답 여부
     */
    public void updateMultipleChoiceGradingResult(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

}
