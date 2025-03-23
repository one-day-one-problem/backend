package site.haruhana.www.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import site.haruhana.www.dto.BaseResponse;

import java.io.IOException;

/**
 * 인증되지 않은 사용자의 보호된 리소스 접근 시 처리를 담당하는 클래스
 * <p>
 * 인증이 필요한 엔드포인트에 인증 없이 접근할 경우 401 Unauthorized 응답을 반환한다. (JSON 형식)
 * 주로 토큰이 없거나 유효하지 않은 경우, 또는 인증 정보가 누락된 경우에 호출된다.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // HTTP 응답 설정
        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 상태 코드 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // Content-Type 설정

        // 오류 응답 객체 생성
        BaseResponse<Void> errorResponse = BaseResponse.onUnauthorized("인증이 필요합니다. 로그인 후 이용해주세요.");

        // 응답 본문에 JSON 작성
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

}
