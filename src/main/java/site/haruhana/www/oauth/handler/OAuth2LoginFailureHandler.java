package site.haruhana.www.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import site.haruhana.www.dto.BaseResponse;

import java.io.IOException;

/**
 * OAuth2 로그인 실패 시 처리를 담당하는 핸들러
 * 인증 실패 시 적절한 에러 메시지를 클라이언트에게 반환
 */
@Slf4j
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    /**
     * OAuth2 로그인 실패 시 호출되는 메소드
     *
     * @param request   HTTP 요청 객체
     * @param response  HTTP 응답 객체
     * @param exception 발생한 인증 예외
     * @throws IOException      입출력 예외 발생 시
     * @throws ServletException 서블릿 예외 발생 시
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        // TODO: 추후 로그인 실패에 대한 기획이 명확해지면 수정 필요

        // 로그인 실패 로그 기록
        log.error("소셜 로그인 실패: {}", exception.getMessage());

        // JSON 형식으로 응답 설정
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 에러 응답 생성
        BaseResponse<?> errorResponse = BaseResponse.onUnauthorized(
                exception.getMessage() != null ? exception.getMessage() : "소셜 로그인 인증에 실패했습니다."
        );

        // JSON 형식으로 에러 응답 전송
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }
}
