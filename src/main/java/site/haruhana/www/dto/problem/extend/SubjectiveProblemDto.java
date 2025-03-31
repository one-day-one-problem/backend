package site.haruhana.www.dto.problem.extend;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import site.haruhana.www.dto.problem.ProblemDto;
import site.haruhana.www.entity.problem.Problem;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubjectiveProblemDto extends ProblemDto {

    public SubjectiveProblemDto(Problem problem, Boolean isSolved) {
        super(problem, isSolved);
    }
}
