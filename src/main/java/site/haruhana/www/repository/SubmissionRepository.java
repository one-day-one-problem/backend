package site.haruhana.www.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.haruhana.www.dto.submission.response.SubmissionHistoryResponseDto;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.ProblemType;
import site.haruhana.www.entity.submission.Submission;
import site.haruhana.www.entity.user.User;

import java.util.Set;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    /**
     * 특정 사용자가 특정 문제를 해결했는지 확인하는 메서드
     *
     * @param user      확인할 사용자
     * @param problemId 확인할 문제 ID
     * @return 해결한 경우 true, 그렇지 않은 경우 false
     */
    boolean existsByUserAndProblemIdAndIsCorrectTrue(User user, Long problemId);

    /**
     * 특정 사용자가 해결한 모든 문제 ID 집합을 조회하는 메서드
     *
     * @param user 조회할 사용자
     * @return 사용자가 해결한 문제 ID 집합
     */
    @Query("SELECT DISTINCT s.problem.id FROM Submission s WHERE s.user = :user AND s.isCorrect = true")
    Set<Long> findProblemIdsSolvedCorrectlyByUser(@Param("user") User user);

    /**
     * 필터 조건에 맞는 문제 답안 제출 기록을 페이징하여 조회하는 메서드
     *
     * @param userId     사용자 ID
     * @param category   카테고리 필터 (null인 경우 모든 카테고리)
     * @param difficulty 난이도 필터 (null인 경우 모든 난이도)
     * @param type       문제 유형 필터 (null인 경우 모든 유형)
     * @param isCorrect  정답 여부 필터 (null인 경우 모든 제출)
     * @param pageable   페이징 정보
     * @return 필터링된 문제 답안 제출 기록 페이지
     */
    @Query("""
            SELECT new site.haruhana.www.dto.submission.response.SubmissionHistoryResponseDto(
                p.id,
                p.title,
                p.category,
                p.difficulty,
                p.type,
                s.submittedAt,
                s.isCorrect,
                s.score
            )
            FROM Submission s JOIN s.problem p
            WHERE s.user.id = :userId
            AND (:category IS NULL OR p.category = :category)
            AND (:difficulty IS NULL OR p.difficulty = :difficulty)
            AND (:type IS NULL OR p.type = :type)
            AND (:isCorrect IS NULL OR s.isCorrect = :isCorrect)
            ORDER BY s.submittedAt DESC
            """)
    Page<SubmissionHistoryResponseDto> findSubmissionsWithFiltersDirect(
            @Param("userId") Long userId,
            @Param("category") ProblemCategory category,
            @Param("difficulty") ProblemDifficulty difficulty,
            @Param("type") ProblemType type,
            @Param("isCorrect") Boolean isCorrect,
            Pageable pageable
    );
}
