package site.haruhana.www.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.haruhana.www.dto.problem.ProblemDto;
import site.haruhana.www.dto.problem.ProblemSortType;
import site.haruhana.www.dto.problem.ProblemSummaryDto;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.exception.ProblemNotFoundException;
import site.haruhana.www.repository.ProblemRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;

    /**
     * 문제 목록을 조회하는 메서드
     *
     * @param page       페이지 번호
     * @param size       페이지 크기
     * @param category   문제 카테고리
     * @param difficulty 문제 난이도
     * @param sortType   문제 정렬 기준
     * @return {@link Page<ProblemSummaryDto>} 문제 목록
     */
    @Transactional(readOnly = true)
    public Page<ProblemSummaryDto> getProblems(int page, int size, ProblemCategory category, ProblemDifficulty difficulty, ProblemSortType sortType) {
        return problemRepository.findProblemsWithFilters(
                PageRequest.of(page, size, sortType.getSort()),
                category,
                difficulty
        );
    }

    /**
     * 특정 문제를 조회하는 메서드
     *
     * @param problemId 조회하고자 하는 문제의 ID
     * @return {@link ProblemDto} 문제 상세 정보
     */
    @Transactional(readOnly = true)
    public ProblemDto getProblem(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(ProblemNotFoundException::new);

        return ProblemDto.from(problem);
    }

}
