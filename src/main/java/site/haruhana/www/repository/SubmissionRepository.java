package site.haruhana.www.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.haruhana.www.entity.submission.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

}
