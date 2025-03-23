package site.haruhana.www.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.haruhana.www.entity.submission.Submission;
import site.haruhana.www.entity.user.User;

import java.util.List;

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
     * 특정 사용자가 해결한 모든 문제 ID 목록을 조회하는 메서드
     *
     * @param user 조회할 사용자
     * @return 사용자가 해결한 문제 ID 목록
     */
    @Query("SELECT DISTINCT s.problem.id FROM Submission s WHERE s.user = :user AND s.isCorrect = true")
    List<Long> findProblemIdsSolvedCorrectlyByUser(@Param("user") User user);

}
