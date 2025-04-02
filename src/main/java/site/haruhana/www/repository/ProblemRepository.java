package site.haruhana.www.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.haruhana.www.dto.problem.ProblemSummaryDto;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.ProblemType;

import java.util.Set;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    /**
     * 필터 조건에 맞는 문제를 페이징하여 조회하는 메서드
     *
     * @param pageable 페이징 정보
     * @param category 문제 카테고리
     * @param difficulty 문제 난이도
     * @param type 문제 유형
     * @param excludeProblemIds 제외할 문제 ID 집합
     */
    @Query("""
            SELECT new site.haruhana.www.dto.problem.ProblemSummaryDto(
                p.id,
                p.title,
                p.category,
                p.difficulty,
                p.type,
                p.solvedCount
            )
            FROM Problem p
            WHERE (:category IS NULL OR p.category = :category)
            AND (:difficulty IS NULL OR p.difficulty = :difficulty)
            AND (:type IS NULL OR p.type = :type)
            AND p.id NOT IN :excludeProblemIds
            """)
    Page<ProblemSummaryDto> findProblemsWithFilters(
            Pageable pageable,
            @Param("category") ProblemCategory category,
            @Param("difficulty") ProblemDifficulty difficulty,
            @Param("type") ProblemType type,
            @Param("excludeProblemIds") Set<Long> excludeProblemIds
    );
}
