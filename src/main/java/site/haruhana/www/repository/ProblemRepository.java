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

public interface ProblemRepository extends JpaRepository<Problem, Long> {

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
            """)
    Page<ProblemSummaryDto> findProblemsWithFilters(
            Pageable pageable,
            @Param("category") ProblemCategory category,
            @Param("difficulty") ProblemDifficulty difficulty,
            @Param("type") ProblemType type
    );
}
