package site.haruhana.www.entity.submission;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.haruhana.www.entity.user.User;
import site.haruhana.www.entity.problem.Problem;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "attempts")
public class Submission {

    /**
     * 시도 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 시도한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    /**
     * 사용자가 시도한 문제
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Problem problem;

    /**
     * 사용자가 문제 풀이를 시도한 시각
     */
    @Column(name = "tried_at", nullable = false)
    private LocalDateTime triedAt;

    /**
     * 사용자가 문제 풀이에 소요된 시간(초)
     */
    private Integer duration;

    /**
     * 사용자가 제출한 답안
     */
    @Column(columnDefinition = "TEXT")
    private String submittedAnswer; // 객관식인 경우에도 String으로 저장. 복수 정답인 경우 콤마(,)로 구분

    /**
     * 사용자가 제출한 답안의 정답 여부
     */
    private Boolean isCorrect;

    /**
     * (주관식 문제에 한함) 사용자가 제출한 답안에 대한 피드백 (by AI)
     */
    @Column(columnDefinition = "TEXT")
    private String feedback;

    /**
     * 피드백이 제공된 시각
     */
    private LocalDateTime feedbackProvidedAt;

}
