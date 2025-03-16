package site.haruhana.www.dto.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.ProblemType;

@Getter
@Builder
@AllArgsConstructor
public class ProblemSummaryDto {
    private Long id;
    private String title;
    private ProblemCategory category;
    private ProblemDifficulty difficulty;
    private ProblemType type;
    private long solvedCount;
}
