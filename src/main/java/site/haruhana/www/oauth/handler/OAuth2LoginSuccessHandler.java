package site.haruhana.www.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import site.haruhana.www.dto.BaseResponse;
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

    private final ObjectMapper objectMapper;

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
        // TODO: 추후 로그인 성공에 대한 기획이 명확해지면 수정 필요

        // CustomOAuth2User로 타입 변환하여 사용자 정보 추출
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        try {
            // JWT 토큰 생성
            TokenDto tokenDto = jwtUtil.generateTokens(oAuth2User.getUserId(), oAuth2User.getRole());

            // 성공 응답 생성
            BaseResponse<TokenDto> baseResponse = BaseResponse.onSuccess("소셜 로그인 성공", tokenDto);

            // JSON 형식으로 응답 전송
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(baseResponse));

        } catch (Exception e) {
            // 토큰 생성 실패 시 에러 처리
            log.error("OAuth2 로그인 처리 중 오류 발생: {}", e.getMessage());
            BaseResponse<?> errorResponse = BaseResponse.onUnauthorized("인증에 실패했습니다.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}
