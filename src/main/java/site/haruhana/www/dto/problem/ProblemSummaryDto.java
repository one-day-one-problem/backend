package site.haruhana.www.dto.problem;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.ProblemType;

@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemSummaryDto {
    private Long id;
    private String title;
    private ProblemCategory category;
    private ProblemDifficulty difficulty;
    private ProblemType type;
    private long solvedCount;
    private Boolean isSolved; // 로그인한 사용자가 문제를 해결했는지 여부

    /**
     * 기본 필드만 포함하는 생성자 (JPQL 쿼리용)
     *
     * @param id          문제 ID
     * @param title       문제 제목
     * @param category    문제 카테고리
     * @param difficulty  문제 난이도
     * @param type        문제 유형
     * @param solvedCount 문제 풀이 수
     */
    public ProblemSummaryDto(Long id, String title, ProblemCategory category, ProblemDifficulty difficulty, ProblemType type, long solvedCount) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.difficulty = difficulty;
        this.type = type;
        this.solvedCount = solvedCount;
        this.isSolved = null;
    }

}
