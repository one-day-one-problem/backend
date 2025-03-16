package site.haruhana.www.dto.problem;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.ProblemType;
import site.haruhana.www.entity.problem.choice.ProblemOption;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDto {
    private final Long id;
    private final String title;
    private final String question;
    private final ProblemCategory category;
    private final ProblemDifficulty difficulty;
    private final ProblemType type;
    private final long solvedCount;

    // 객관식 문제 전용 필드
    private final List<ProblemOptionDto> options;

    // 주관식 문제 전용 필드
    private final String expectedAnswerLength;

    /**
     * Problem 엔티티를 DTO로 변환하는 생성자
     *
     * @param problem 변환할 Problem 엔티티
     */
    public ProblemDto(Problem problem) {
        this.id = problem.getId();
        this.title = problem.getTitle();
        this.question = problem.getQuestion();
        this.category = problem.getCategory();
        this.difficulty = problem.getDifficulty();
        this.type = problem.getType();
        this.solvedCount = problem.getSolvedCount();

        if (problem.getType() == ProblemType.MULTIPLE_CHOICE) { // 객관식 문제인 경우
            this.options = problem.getProblemOptions().stream()
                    .map(ProblemOptionDto::new)
                    .toList();
            this.expectedAnswerLength = null;

        } else { // 주관식 문제인 경우
            this.options = null;
            this.expectedAnswerLength = problem.getExpectedAnswerLength();
        }
    }

    /**
     * Problem 엔티티를 DTO로 변환하는 정적 팩토리 메서드
     *
     * @param problem 변환할 Problem 엔티티
     * @return ProblemDto
     */
    public static ProblemDto from(Problem problem) {
        return new ProblemDto(problem);
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProblemOptionDto {
        private final long id;
        private final String content;

        public ProblemOptionDto(ProblemOption option) {
            this.id = option.getId();
            this.content = option.getContent();
        }
    }
}
