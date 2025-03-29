package site.haruhana.www.oauth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import site.haruhana.www.dto.user.TokenDto;
import site.haruhana.www.oauth.CustomOAuth2User;
import site.haruhana.www.utils.JwtUtil;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 시 처리를 담당하는 핸들러
 * 소셜 로그인 성공 후 JWT 토큰을 생성하여 클라이언트에게 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.frontend.oauth-callback-path}")
    private String oauthCallbackPath; // 프론트엔드의 OAuth 콜백 경로

    @Value("${app.frontend.redirect-path}")
    private String redirectPath; // 프론트엔드의 리다이렉트 경로

    /**
     * OAuth2 로그인 성공 시 호출되는 메소드
     *
     * @param request        HTTP 요청 객체
     * @param response       HTTP 응답 객체
     * @param authentication 인증 정보가 담긴 객체
     * @throws IOException 입출력 예외 발생 시
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // CustomOAuth2User로 타입 변환하여 사용자 정보 추출
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        try {
            // JWT 토큰 생성
            TokenDto tokenDto = jwtUtil.generateTokens(oAuth2User.getUserId(), oAuth2User.getRole());

            // 프론트엔드 OAuth 콜백 페이지로 리다이렉트 URL 생성
            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + oauthCallbackPath)
                    .queryParam("accessToken", tokenDto.getAccessToken())
                    .queryParam("refreshToken", tokenDto.getRefreshToken())
                    .queryParam("redirectTo", redirectPath)
                    .build()
                    .toUriString();

            // 클라이언트 리다이렉트
            log.info("OAuth2 로그인 성공: 프론트엔드로 리다이렉트 - {}", frontendUrl + oauthCallbackPath);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            // 토큰 생성 실패 시 에러 처리
            log.error("OAuth2 로그인 처리 중 오류 발생: {}", e.getMessage(), e);

            String failureUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login")
                    .queryParam("error", "true")
                    .queryParam("message", "인증 처리 중 오류가 발생했습니다.")
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, failureUrl);
        }
    }
}
