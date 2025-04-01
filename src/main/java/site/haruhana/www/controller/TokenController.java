package site.haruhana.www.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.haruhana.www.dto.BaseResponse;
import site.haruhana.www.dto.user.TokenDto;
import site.haruhana.www.dto.user.TokenRefreshRequestDto;
import site.haruhana.www.utils.JwtUtil;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TokenController {

    private final JwtUtil jwtUtil;

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenDto>> refreshToken(@RequestBody TokenRefreshRequestDto requestDto) {
        TokenDto newTokens = jwtUtil.refreshTokens(requestDto.getRefreshToken());
        return ResponseEntity.ok(BaseResponse.onSuccess("토큰이 성공적으로 갱신되었습니다.", newTokens));
    }
}
