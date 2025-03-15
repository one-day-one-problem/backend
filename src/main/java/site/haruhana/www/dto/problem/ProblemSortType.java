package site.haruhana.www.dto.problem;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum ProblemSortType {
    LATEST("createdAt", Sort.Direction.DESC),
    OLDEST("createdAt", Sort.Direction.ASC),
    MOST_SOLVED("solvedCount", Sort.Direction.DESC),
    LEAST_SOLVED("solvedCount", Sort.Direction.ASC);

    private final String field;
    private final Sort.Direction direction;

    public Sort getSort() {
        return Sort.by(direction, field);
    }
}
