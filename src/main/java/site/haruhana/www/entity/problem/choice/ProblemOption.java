package site.haruhana.www.entity.problem.choice;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.haruhana.www.entity.problem.Problem;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "problem_options")
public class ProblemOption {

    /**
     * 문제와 옵션의 관계식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 문제
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Problem problem;

    /**
     * 옵션 내용
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 해당 옵션이 문제의 정답인지 여부
     */
    private boolean isCorrect;

}
