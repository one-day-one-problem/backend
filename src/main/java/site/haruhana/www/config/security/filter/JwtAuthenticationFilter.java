package site.haruhana.www.config.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import site.haruhana.www.repository.UserRepository;
import site.haruhana.www.utils.JwtUtil;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 토큰을 검증하고 사용자 인증 정보를 설정하는 필터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // 요청 헤더에서 JWT 토큰 추출
            String token = extractToken(request);

            // 토큰이 유효한 경우 인증 정보 설정
            if (token != null && jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);

                // 사용자 정보 조회
                userRepository.findById(userId).ifPresent(user -> {
                    // 인증 정보 생성
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())));

                    // SecurityContextHolder에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("사용자 '{}' 인증 성공", user.getId());
                });
            }

        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage());
            // 인증 오류가 발생해도 필터 체인은 계속 진행 (인증되지 않은 상태로)
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HttpServletRequest에서 Authorization 헤더의 Bearer 토큰을 추출하는 메서드
     *
     * @param request HTTP 요청 객체
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

}
