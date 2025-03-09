package site.haruhana.www.entity.problem;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProblemStatus {
    ACTIVE("활성"), // 정상적으로 사용 가능한 상태
    INACTIVE("비활성"), // 일시적으로 사용 중지된 상태
    UNDER_REVIEW("검토중"), // 피드백으로 인해 검토가 필요한 상태
    DEPRECATED("폐기"); // 더 이상 사용하지 않는 상태

    private final String description;
}
