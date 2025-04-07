package site.haruhana.www.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.haruhana.www.dto.BaseResponse;
import site.haruhana.www.dto.submission.SubmissionPage;
import site.haruhana.www.dto.submission.request.SubmissionRequestDto;
import site.haruhana.www.dto.submission.response.SubmissionHistoryResponseDto;
import site.haruhana.www.dto.submission.response.SubmissionResponseDto;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.ProblemType;
import site.haruhana.www.entity.user.User;
import site.haruhana.www.service.SubmissionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * 문제 답안 제출 API
     *
     * @param problemId  문제 ID (URL 경로 변수)
     * @param user       현재 인증된 사용자
     * @param requestDto 제출 요청 정보
     * @return 제출 결과 정보
     */
    @PostMapping("/problems/{problemId}/submissions")
    public ResponseEntity<BaseResponse<SubmissionResponseDto>> submitAnswer(
            @AuthenticationPrincipal @NotNull User user,
            @PathVariable("problemId") Long problemId,
            @Valid @RequestBody SubmissionRequestDto requestDto
    ) {
        SubmissionResponseDto data = submissionService.submitAnswer(user, problemId, requestDto);
        return ResponseEntity.ok(BaseResponse.onSuccess("답안이 제출되었습니다.", data));
    }

    /**
     * 사용자의 문제 풀이 기록 조회 API
     *
     * @param userId     조회할 사용자의 ID (URL 경로 변수)
     * @param page       페이지 번호 (기본값: 0)
     * @param size       페이지 크기 (기본값: 10)
     * @param category   문제 카테고리 필터 (선택 사항)
     * @param difficulty 문제 난이도 필터 (선택 사항)
     * @param type       문제 유형 필터 (선택 사항)
     * @param isCorrect  정답 여부 필터 (선택 사항)
     * @return 필터링된 페이징 처리된 사용자의 문제 풀이 기록 목록
     */
    @GetMapping("/users/{userId}/submissions")
    @PreAuthorize("isAuthenticated() and #userId == authentication.principal.id")
    public ResponseEntity<BaseResponse<SubmissionPage<SubmissionHistoryResponseDto>>> getSubmissionHistory(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) ProblemCategory category,
            @RequestParam(required = false) ProblemDifficulty difficulty,
            @RequestParam(required = false) ProblemType type,
            @RequestParam(required = false) Boolean isCorrect
    ) {
        SubmissionPage<SubmissionHistoryResponseDto> data = submissionService.getSubmissionHistoryByUser(userId, page, size, category, difficulty, type, isCorrect);
        return ResponseEntity.ok(BaseResponse.onSuccess("문제 풀이 기록이 성공적으로 조회되었습니다.", data));
    }

}
