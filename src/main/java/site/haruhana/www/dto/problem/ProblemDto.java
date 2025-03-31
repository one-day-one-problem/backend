package site.haruhana.www.dto.problem;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import site.haruhana.www.dto.problem.extend.MultipleChoiceProblemDto;
import site.haruhana.www.dto.problem.extend.SubjectiveProblemDto;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.ProblemType;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class ProblemDto {
    private final Long id;
    private final String title;
    private final String question;
    private final ProblemCategory category;
    private final ProblemDifficulty difficulty;
    private final ProblemType type;
    private final long solvedCount;
    private final Boolean isSolved; // 로그인한 사용자가 문제를 해결했는지 여부

    protected ProblemDto(Problem problem, Boolean isSolved) {
        this.id = problem.getId();
        this.title = problem.getTitle();
        this.question = problem.getQuestion();
        this.category = problem.getCategory();
        this.difficulty = problem.getDifficulty();
        this.type = problem.getType();
        this.solvedCount = problem.getSolvedCount();
        this.isSolved = isSolved;
    }

    public static ProblemDto from(Problem problem) {
        return from(problem, null);
    }

    public static ProblemDto from(Problem problem, Boolean isSolved) {
        return switch (problem.getType()) {
            case MULTIPLE_CHOICE -> new MultipleChoiceProblemDto(problem, isSolved);
            case SUBJECTIVE -> new SubjectiveProblemDto(problem, isSolved);
        };
    }

}
