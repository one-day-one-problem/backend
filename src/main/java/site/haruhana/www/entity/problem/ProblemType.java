package site.haruhana.www.entity.problem;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProblemType {
    MULTIPLE_CHOICE("객관식"),
    SUBJECTIVE("주관식");

    private final String description;
}
