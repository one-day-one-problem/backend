package site.haruhana.www.entity.problem;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.haruhana.www.entity.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "problems")
public class Problem extends BaseTimeEntity {

    /**
     * 문제 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 문제 제목
     */
    private String title;

    /**
     * 문제 내용
     */
    @Column(columnDefinition = "TEXT")
    private String question;

    /**
     * 문제 카테고리
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    /**
     * 문제 난이도
     */
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    /**
     * 문제 유형
     */
    @Enumerated(EnumType.STRING)
    private Type type;

    /**
     * (객관식) 문제의 옵션 목록 정보
     */
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemOption> problemOptions = new ArrayList<>();

    /**
     * 문제 제공자
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemProvider problemProvider;

}
