package site.haruhana.www.entity.problem;

import jakarta.persistence.*;
import lombok.*;
import site.haruhana.www.entity.BaseTimeEntity;
import site.haruhana.www.entity.problem.choice.Option;
import site.haruhana.www.entity.problem.choice.ProblemOption;
import site.haruhana.www.entity.problem.feedback.FeedbackType;
import site.haruhana.www.entity.problem.feedback.ProblemFeedback;
import site.haruhana.www.entity.problem.subjective.GradingCriteria;
import site.haruhana.www.entity.user.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "problems")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @Column(nullable = false)
    private String title;

    /**
     * 문제 내용
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    /**
     * 문제 카테고리
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProblemCategory category;

    /**
     * 문제 난이도
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProblemDifficulty difficulty;

    /**
     * 문제 유형
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProblemType type;

    /**
     * 문제 제공자
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemProvider problemProvider;

    /**
     * 문제 상태 (활성/비활성/검토중 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemStatus status = ProblemStatus.ACTIVE;

    /**
     * 문제 피드백 목록
     */
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL)
    @OrderBy("createdAt DESC")
    private List<ProblemFeedback> feedbacks = new ArrayList<>();

    /**
     * (객관식) 문제의 옵션 목록 정보
     */
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemOption> problemOptions = new ArrayList<>();

    /**
     * (주관식) 문제의 채점 기준 목록
     */
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GradingCriteria> gradingCriteria = new ArrayList<>();

    /**
     * (주관식) 예상 답안 길이
     */
    @Column(nullable = true)
    private String expectedAnswerLength;

    /**
     * (주관식) 예시 답안
     */
    @Column(columnDefinition = "TEXT", nullable = true)
    private String sampleAnswer;

    @Builder(builderClassName = "MultipleChoiceProblemBuilder")
    private Problem(String title, String question, ProblemCategory category, ProblemDifficulty difficulty, ProblemProvider provider) {
        this.title = title;
        this.question = question;
        this.category = category;
        this.difficulty = difficulty;
        this.type = ProblemType.MULTIPLE_CHOICE;
        this.problemProvider = provider;
        this.status = ProblemStatus.ACTIVE;
        this.problemOptions = new ArrayList<>();
    }

    @Builder(builderClassName = "SubjectiveProblemBuilder")
    private Problem(String title, String question, ProblemCategory category, ProblemDifficulty difficulty, ProblemProvider provider, String expectedAnswerLength, String sampleAnswer) {
        this.title = title;
        this.question = question;
        this.category = category;
        this.difficulty = difficulty;
        this.type = ProblemType.SUBJECTIVE;
        this.problemProvider = provider;
        this.expectedAnswerLength = expectedAnswerLength;
        this.sampleAnswer = sampleAnswer;
        this.status = ProblemStatus.ACTIVE;
        this.gradingCriteria = new ArrayList<>();
    }

    public static SubjectiveProblemBuilder subjectiveProblemBuilder() {
        return new SubjectiveProblemBuilder();
    }

    public static MultipleChoiceProblemBuilder multipleChoiceProblemBuilder() {
        return new MultipleChoiceProblemBuilder();
    }

    /**
     * 문제에 옵션을 추가하는 연관관계 편의 메서드
     *
     * @param optionContent 옵션 내용
     * @param isCorrect     정답 여부
     * @throws IllegalStateException 주관식 문제에 옵션을 추가하려 할 때
     */
    public void addOption(String optionContent, boolean isCorrect) {
        if (this.type != ProblemType.MULTIPLE_CHOICE) {
            throw new IllegalStateException("Cannot add options to non-multiple choice problems");
        }

        Option option = Option.builder()
                .content(optionContent)
                .build();

        this.problemOptions.add(
                ProblemOption.builder()
                        .problem(this)
                        .option(option)
                        .isCorrect(isCorrect)
                        .build()
        );
    }

    /**
     * 주관식 문제에 채점 기준을 추가하는 연관관계 편의 메서드
     *
     * @param content 채점 기준 내용
     * @throws IllegalStateException 객관식 문제에 채점 기준을 추가하려 할 때
     */
    public void addGradingCriteria(String content) {
        if (this.type != ProblemType.SUBJECTIVE) {
            throw new IllegalStateException("Cannot add grading criteria to non-subjective problems");
        }

        this.gradingCriteria.add(
                GradingCriteria.builder()
                        .problem(this)
                        .content(content)
                        .build()
        );
    }

    /**
     * 문제에 피드백을 추가하는 연관관계 편의 메서드
     *
     * @param content  피드백 내용
     * @param type     피드백 유형 (오타, 부정확한 내용, 모호한 표현 등)
     * @param reporter 피드백 제출자
     */
    public void addFeedback(String content, FeedbackType type, User reporter) {
        ProblemFeedback feedback = ProblemFeedback.builder()
                .problem(this)
                .content(content)
                .type(type)
                .reporter(reporter)
                .build();

        this.feedbacks.add(feedback);
    }

}
