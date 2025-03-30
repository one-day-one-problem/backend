package site.haruhana.www.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.haruhana.www.dto.BaseResponse;
import site.haruhana.www.dto.submission.request.SubmissionRequestDto;
import site.haruhana.www.dto.submission.response.SubmissionResponseDto;
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
            @AuthenticationPrincipal User user,
            @PathVariable("problemId") Long problemId,
            @Valid @RequestBody SubmissionRequestDto requestDto
    ) {
        SubmissionResponseDto data = submissionService.submitAnswer(user, problemId, requestDto);
        return ResponseEntity.ok(BaseResponse.onSuccess("답안이 제출되었습니다.", data));
    }
}
