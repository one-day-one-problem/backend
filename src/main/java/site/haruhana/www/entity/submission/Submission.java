package site.haruhana.www.entity.submission;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.submission.choice.MultipleChoiceSubmission;
import site.haruhana.www.entity.submission.subjective.SubjectiveSubmission;
import site.haruhana.www.entity.user.User;

import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@Table(name = "submissions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "submission_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Submission {

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
    private String submittedAnswer;

    /**
     * 사용자가 제출한 답안의 정답 여부
     */
    @Column(name = "is_correct")
    private Boolean isCorrect;

    /**
     * 제출 타입에 따른 엔티티 생성 팩토리 메서드
     */
    public static Submission from(User user, Problem problem, String submittedAnswer, int duration) {
        return switch (problem.getType()) {
            case MULTIPLE_CHOICE -> MultipleChoiceSubmission.builder()
                    .user(user)
                    .problem(problem)
                    .submittedAt(LocalDateTime.now())
                    .duration(duration)
                    .submittedAnswer(submittedAnswer)
                    .build();
            case SUBJECTIVE -> SubjectiveSubmission.builder()
                    .user(user)
                    .problem(problem)
                    .submittedAt(LocalDateTime.now())
                    .duration(duration)
                    .submittedAnswer(submittedAnswer)
                    .build();
        };
    }

    /**
     * 정답 여부 확인 메소드 (모든 타입의 제출에서 공통으로 사용됨)
     */
    public Boolean getIsCorrect() {
        return this.isCorrect;
    }

    /**
     * 정답 여부 설정 메소드
     */
    protected void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
}
