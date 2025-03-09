package site.haruhana.www.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.haruhana.www.entity.problem.Problem;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

}
