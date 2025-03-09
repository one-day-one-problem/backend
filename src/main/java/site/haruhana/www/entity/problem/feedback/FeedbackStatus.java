package site.haruhana.www.entity.problem.feedback;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedbackStatus {
    PENDING("대기중"),
    IN_PROGRESS("처리중"),
    RESOLVED("해결됨"),
    REJECTED("반려됨");

    private final String description;
}
