package site.haruhana.www.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.haruhana.www.base.BaseResponse;
import site.haruhana.www.dto.submission.request.SubmissionRequestDto;
import site.haruhana.www.dto.submission.response.SubmissionResponseDto;
import site.haruhana.www.entity.user.User;
import site.haruhana.www.service.SubmissionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * 문제 답안 제출 API
     *
     * @param user       현재 인증된 사용자
     * @param requestDto 제출 요청 정보
     * @return 제출 결과 정보
     */
    @PostMapping
    public ResponseEntity<BaseResponse<SubmissionResponseDto>> submitAnswer(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SubmissionRequestDto requestDto
    ) {
        SubmissionResponseDto data = submissionService.submitAnswer(user, requestDto);
        return ResponseEntity.ok(BaseResponse.onSuccess("답안이 제출되었습니다.", data));
    }
}
