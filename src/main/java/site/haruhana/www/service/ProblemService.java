package site.haruhana.www.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.haruhana.www.dto.problem.ProblemDto;
import site.haruhana.www.dto.problem.ProblemPage;
import site.haruhana.www.dto.problem.ProblemSortType;
import site.haruhana.www.dto.problem.ProblemSummaryDto;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.user.User;
import site.haruhana.www.exception.ProblemNotFoundException;
import site.haruhana.www.repository.ProblemRepository;
import site.haruhana.www.repository.SubmissionRepository;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;

    private final SubmissionRepository submissionRepository;

    /**
     * 문제 목록을 조회하는 메서드
     * <p>
     * 페이징, 카테고리, 난이도 및 정렬 조건에 따라 문제 목록을 조회합니다.
     * 인증된 사용자인 경우 각 문제에 대한 사용자의 정확한 해결 여부도 함께 제공합니다.
     *
     * @param page       페이지 번호
     * @param size       페이지 크기
     * @param category   문제 카테고리 (필터링 조건)
     * @param difficulty 문제 난이도 (필터링 조건)
     * @param sortType   문제 정렬 기준
     * @param user       현재 사용자 정보 (null인 경우 해결 정보 제외)
     * @return 문제 목록 (페이징 정보 포함)
     */
    @Transactional(readOnly = true)
    public ProblemPage<ProblemSummaryDto> getProblems(int page, int size, ProblemCategory category, ProblemDifficulty difficulty, ProblemSortType sortType, User user) {
        Page<ProblemSummaryDto> problems = problemRepository.findProblemsWithFilters(
                PageRequest.of(page, size, sortType.getSort()),
                category,
                difficulty
        );

        // 인증된 사용자가 있는 경우, 사용자가 해결했는지 여부 확인
        if (user != null && !problems.isEmpty()) {
            // 사용자가 해결한 문제 목록을 가져옴
            List<Long> correctlySolvedProblemIds = submissionRepository.findProblemIdsSolvedCorrectlyByUser(user);
            Set<Long> correctlySolvedProblemIdSet = Set.copyOf(correctlySolvedProblemIds);

            // 각 문제별 정확한 해결 여부 설정
            for (ProblemSummaryDto problem : problems.getContent()) {
                boolean isSolvedCorrectly = correctlySolvedProblemIdSet.contains(problem.getId());
                problem.setIsSolved(isSolvedCorrectly);
            }
        }

        return new ProblemPage<>(problems);
    }

    /**
     * 특정 문제의 상세 정보를 조회하는 메서드
     * <p>
     * 문제 ID에 해당하는 문제의 상세 정보를 조회합니다.
     * 인증된 사용자인 경우 해당 문제에 대한 사용자의 정확한 해결 여부도 함께 제공합니다.
     *
     * @param problemId 조회할 문제의 ID
     * @param user      현재 사용자 정보 (null인 경우 해결 정보 제외)
     * @return 문제의 상세 정보
     * @throws ProblemNotFoundException 문제를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public ProblemDto getProblem(Long problemId, User user) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(ProblemNotFoundException::new);

        // 인증된 사용자가 있는 경우, 문제를 해결했는지 여부 확인
        if (user != null) {
            boolean isSolved = submissionRepository.existsByUserAndProblemIdAndIsCorrectTrue(user, problemId);
            return ProblemDto.from(problem, isSolved);
        }

        return ProblemDto.from(problem);
    }
}
