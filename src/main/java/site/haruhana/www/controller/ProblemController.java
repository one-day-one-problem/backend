package site.haruhana.www.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.haruhana.www.dto.BaseResponse;
import site.haruhana.www.dto.problem.ProblemDto;
import site.haruhana.www.dto.problem.ProblemPage;
import site.haruhana.www.dto.problem.ProblemSortType;
import site.haruhana.www.dto.problem.ProblemSummaryDto;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.ProblemType;
import site.haruhana.www.entity.user.User;
import site.haruhana.www.service.ProblemService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    /**
     * 문제 목록 조회 API
     * <p>
     * 페이징, 카테고리, 난이도, 유형 및 정렬 조건에 따라 문제 목록을 조회합니다.
     * 로그인한 사용자가 요청한 경우 각 문제에 대한 사용자의 해결 여부도 함께 제공합니다.
     *
     * @param page       페이지 번호 (기본값: 0)
     * @param size       페이지 크기 (기본값: 12)
     * @param category   문제 카테고리 (선택 사항)
     * @param difficulty 문제 난이도 (선택 사항)
     * @param type       문제 유형 (선택 사항)
     * @param sortType   문제 정렬 기준 (기본값: 가장 많이 푼 문제)
     * @param user       현재 인증된 사용자 (인증되지 않은 경우 null)
     * @return 문제 목록 응답
     */
    @GetMapping
    public ResponseEntity<BaseResponse<ProblemPage<ProblemSummaryDto>>> getProblems(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) int size,
            @RequestParam(required = false) ProblemCategory category,
            @RequestParam(required = false) ProblemDifficulty difficulty,
            @RequestParam(required = false) ProblemType type,
            @RequestParam(defaultValue = "MOST_SOLVED") ProblemSortType sortType,
            @AuthenticationPrincipal User user
    ) {
        ProblemPage<ProblemSummaryDto> data = problemService.getProblems(page, size, category, difficulty, type, sortType, user);
        return ResponseEntity.ok(BaseResponse.onSuccess("문제 목록을 조회하는데 성공했습니다.", data));
    }

    /**
     * 특정 문제 상세 조회 API
     * <p>
     * 특정 문제의 상세 정보를 조회합니다.
     * 로그인한 사용자가 요청한 경우 해당 문제에 대한 사용자의 해결 여부도 함께 제공합니다.
     *
     * @param problemId 조회할 문제 ID
     * @param user      현재 인증된 사용자 (인증되지 않은 경우 null)
     * @return 문제 상세 정보 응답
     */
    @GetMapping("/{problemId}")
    public ResponseEntity<BaseResponse<ProblemDto>> getProblem(
            @PathVariable Long problemId,
            @AuthenticationPrincipal User user
    ) {
        ProblemDto data = problemService.getProblem(problemId, user);
        return ResponseEntity.ok(BaseResponse.onSuccess("문제를 조회하는데 성공했습니다.", data));
    }
}
