package site.haruhana.www.oauth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 로그인 실패 시 처리를 담당하는 핸들러
 * 인증 실패 시 적절한 에러 메시지를 클라이언트에게 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * OAuth2 로그인 실패 시 호출되는 메소드
     *
     * @param request   HTTP 요청 객체
     * @param response  HTTP 응답 객체
     * @param exception 발생한 인증 예외
     * @throws IOException      입출력 예외 발생 시
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        // 로그인 실패 로그 기록
        log.error("소셜 로그인 실패: {}", exception.getMessage());
        
        // 에러 메시지 인코딩
        String errorMessage = exception.getMessage() != null ? exception.getMessage() : "소셜 로그인 인증에 실패했습니다.";
        String encodedErrorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        
        // 프론트엔드 로그인 페이지로 리다이렉트 URL 생성
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login")
                .queryParam("error", "true")
                .queryParam("message", encodedErrorMessage)
                .build()
                .toUriString();
        
        // 클라이언트 리다이렉트
        log.info("OAuth2 로그인 실패: 프론트엔드로 리다이렉트 - {}", frontendUrl + "/login");
        response.sendRedirect(targetUrl);
    }
}
