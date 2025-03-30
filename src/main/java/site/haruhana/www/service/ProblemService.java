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
import site.haruhana.www.entity.problem.ProblemType;
import site.haruhana.www.entity.user.User;
import site.haruhana.www.exception.ProblemNotFoundException;
import site.haruhana.www.repository.ProblemRepository;
import site.haruhana.www.repository.SubmissionRepository;

import java.util.Collections;
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
     * 페이징, 카테고리, 난이도, 유형 및 정렬 조건에 따라 문제 목록을 조회합니다.
     * 필터링 조건에 맞는 문제만 반환하며, 미해결 문제만 보기 옵션이 활성화된 경우 사용자가 아직 해결하지 않은 문제만 조회합니다.
     * 인증된 사용자인 경우 각 문제에 대한 사용자의 정확한 해결 여부 정보도 함께 제공합니다.
     *
     * @param page         페이지 번호 (0부터 시작)
     * @param size         페이지당 문제 수
     * @param category     문제 카테고리 필터 (null인 경우 모든 카테고리)
     * @param difficulty   문제 난이도 필터 (null인 경우 모든 난이도)
     * @param type         문제 유형 필터 (null인 경우 모든 유형)
     * @param sortType     문제 정렬 기준 (최신순, 오래된순, 많이 푼 순, 적게 푼 순)
     * @param onlyUnsolved 미해결 문제만 조회 여부 (true: 미해결 문제만, false: 모든 문제)
     * @param user         현재 사용자 정보 (null인 경우 인증되지 않은 사용자)
     * @return 페이징 처리된 문제 목록과 페이지 정보
     */
    @Transactional(readOnly = true)
    public ProblemPage<ProblemSummaryDto> getProblems(int page, int size, ProblemCategory category, ProblemDifficulty difficulty, ProblemType type, ProblemSortType sortType, boolean onlyUnsolved, User user) {
        // 사용자 인증 여부에 따라 미해결 문제만 조회 옵션 처리
        boolean applyUnsolvedFilter = shouldApplyUnsolvedFilter(user, onlyUnsolved);

        // 페이지 요청 객체 생성
        PageRequest pageRequest = PageRequest.of(page, size, sortType.getSort());

        // 문제 목록
        Page<ProblemSummaryDto> problems;

        if (user != null) { // 인증된 사용자의 요청인 경우
            // 사용자가 해결한 문제 ID 집합 조회
            Set<Long> solvedProblemIds = getSolvedProblemIds(user);

            // 문제 목록 조회 및 해결 여부 설정
            problems = fetchProblems(pageRequest, category, difficulty, type, applyUnsolvedFilter, solvedProblemIds);
            if (!problems.isEmpty()) {
                markProblemSolvedStatus(problems.getContent(), solvedProblemIds, applyUnsolvedFilter);
            }

        } else {
            problems = fetchProblems(pageRequest, category, difficulty, type, applyUnsolvedFilter, Collections.emptySet());
        }

        return new ProblemPage<>(problems);
    }

    /**
     * 미해결 문제만 보기 필터를 적용해야 하는지 판단하는 메서드
     *
     * @param user         현재 사용자 정보
     * @param onlyUnsolved 미해결 문제만 조회 요청 여부
     * @return 미해결 문제 필터 적용 여부 (true: 적용, false: 미적용)
     */
    private boolean shouldApplyUnsolvedFilter(User user, boolean onlyUnsolved) {
        if (user == null && onlyUnsolved) {
            log.warn("인증되지 않은 사용자가 미해결 문제 필터링을 요청했습니다. 해당 필터는 무시됩니다.");
            return false;
        }

        return user != null && onlyUnsolved;
    }

    /**
     * 사용자가 해결한 문제 ID 집합을 조회하는 메서드
     *
     * @param user 사용자 정보
     * @return 사용자가 정확하게 해결한 문제 ID 집합 (Set)
     */
    private Set<Long> getSolvedProblemIds(User user) {
        if (user == null) {
            return Collections.emptySet();
        }

        return submissionRepository.findProblemIdsSolvedCorrectlyByUser(user);
    }

    /**
     * 필터 조건에 맞는 문제 목록을 조회하는 메서드
     * <p>
     * 미해결 문제만 보기 옵션이 활성화된 경우, 사용자가 해결한 문제 ID를 제외한 결과를 반환합니다.
     *
     * @param pageRequest           페이지 요청 객체 (페이지 번호, 크기, 정렬 정보 포함)
     * @param category              문제 카테고리 필터
     * @param difficulty            문제 난이도 필터
     * @param type                  문제 유형 필터
     * @param excludeSolvedProblems 미해결 문제만 조회 여부
     * @param excludeProblemIds     제외할 문제 ID 집합 (사용자가 해결한 문제 ID 집합)
     * @return 페이징된 문제 목록
     */
    private Page<ProblemSummaryDto> fetchProblems(PageRequest pageRequest, ProblemCategory category, ProblemDifficulty difficulty, ProblemType type, boolean excludeSolvedProblems, Set<Long> excludeProblemIds) {
        return problemRepository.findProblemsWithFilters(
                pageRequest,
                category,
                difficulty,
                type,
                excludeSolvedProblems ? excludeProblemIds : Collections.emptySet()
        );
    }

    /**
     * 문제 목록의 해결 여부 정보를 설정하는 메서드
     *
     * @param problems         해결 여부를 설정할 문제 목록
     * @param solvedProblemIds 사용자가 해결한 문제 ID 집합
     * @param onlyUnsolved     미해결 문제만 조회 여부 (true: 미해결 문제만 조회, false: 일반 조회)
     */
    private void markProblemSolvedStatus(List<ProblemSummaryDto> problems, Set<Long> solvedProblemIds, boolean onlyUnsolved) {
        if (onlyUnsolved) { // 미해결 문제만 조회하는 경우
            problems.forEach(problem ->
                    problem.setIsSolved(false) // 모든 문제를 미해결 상태로 설정
            );

        } else { // 일반 조회인 경우 (미해결 문제 + 해결 문제 모두 조회하는 경우)
            problems.forEach(problem ->
                    problem.setIsSolved(solvedProblemIds.contains(problem.getId())) // 사용자가 해결한 문제만 해결 상태로 설정
            );
        }
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
     * @throws ProblemNotFoundException 문제 ID에 해당하는 문제를 찾을 수 없는 경우
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
