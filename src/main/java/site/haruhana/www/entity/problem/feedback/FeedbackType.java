package site.haruhana.www.entity.problem.feedback;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedbackType {
    TYPO("오타"),
    INCORRECT_CONTENT("부정확한 내용"),
    AMBIGUOUS_EXPRESSION("모호한 표현"),
    INCORRECT_ANSWER("잘못된 정답"),
    DUPLICATE_QUESTION("중복된 문제"),
    INAPPROPRIATE_DIFFICULTY("부적절한 난이도"),
    OTHER("기타");

    private final String description;
}
