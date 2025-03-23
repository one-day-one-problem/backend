package site.haruhana.www.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import site.haruhana.www.dto.BaseResponse;

import java.io.IOException;

/**
 * 인증된 사용자가 권한이 없는 리소스에 접근 시 처리를 담당하는 클래스
 * <p>
 * 인증은 되었으나 해당 리소스에 대한 접근 권한이 없는 경우 403 Forbidden 응답을 반환한다. (JSON 형식)
 * 주로 권한이 부족한 사용자(예: 일반 USER가 ADMIN 리소스에 접근)가 접근하는 경우에 호출된다.
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        // HTTP 응답 설정
        response.setStatus(HttpStatus.FORBIDDEN.value()); // 상태 코드 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // Content-Type 설정

        // 오류 응답 객체 생성
        BaseResponse<Void> errorResponse = BaseResponse.onForbidden("접근 권한이 없습니다.");

        // 응답 본문에 JSON 작성
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

}
