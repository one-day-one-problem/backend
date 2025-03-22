package site.haruhana.www.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.haruhana.www.dto.BaseResponse;
import site.haruhana.www.dto.problem.ProblemDto;
import site.haruhana.www.dto.problem.ProblemPage;
import site.haruhana.www.dto.problem.ProblemSortType;
import site.haruhana.www.dto.problem.ProblemSummaryDto;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.service.ProblemService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    /**
     * 문제 목록 조회 API
     *
     * @param page       페이지 번호
     * @param size       페이지 크기
     * @param category   문제 카테고리
     * @param difficulty 문제 난이도
     * @param sortType   문제 정렬 기준
     */
    @GetMapping
    public ResponseEntity<BaseResponse<ProblemPage<ProblemSummaryDto>>> getProblems(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) int size,
            @RequestParam(required = false) ProblemCategory category,
            @RequestParam(required = false) ProblemDifficulty difficulty,
            @RequestParam(defaultValue = "MOST_SOLVED") ProblemSortType sortType
    ) {
        Page<ProblemSummaryDto> problems = problemService.getProblems(page, size, category, difficulty, sortType);
        return ResponseEntity.ok(BaseResponse.onSuccess("문제 목록을 조회하는데 성공했습니다.", new ProblemPage<>(problems)));
    }

    @GetMapping("/{problemId}")
    public ResponseEntity<BaseResponse<ProblemDto>> getProblem(@PathVariable Long problemId) {
        ProblemDto problem = problemService.getProblem(problemId);
        return ResponseEntity.ok(BaseResponse.onSuccess("문제를 조회하는데 성공했습니다.", problem));
    }
}
