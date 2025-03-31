package site.haruhana.www.dto.problem.extend;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import site.haruhana.www.dto.problem.ProblemDto;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.problem.choice.ProblemOption;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MultipleChoiceProblemDto extends ProblemDto {
    private final List<ProblemOptionDto> options;

    public MultipleChoiceProblemDto(Problem problem, Boolean isSolved) {
        super(problem, isSolved);
        this.options = problem.getProblemOptions().stream()
                .map(ProblemOptionDto::new)
                .toList();
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
