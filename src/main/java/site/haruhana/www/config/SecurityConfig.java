package site.haruhana.www.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import site.haruhana.www.config.security.filter.JwtAuthenticationFilter;
import site.haruhana.www.config.security.handler.CustomAccessDeniedHandler;
import site.haruhana.www.config.security.handler.CustomAuthenticationEntryPoint;
import site.haruhana.www.oauth.CustomOAuth2UserService;
import site.haruhana.www.oauth.handler.OAuth2LoginFailureHandler;
import site.haruhana.www.oauth.handler.OAuth2LoginSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // Rest API 사용으로 CSRF 비활성화.
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/actuator/health").permitAll() // 로드밸런서 대상그룹 Health Check를 위해 허용
                        .requestMatchers("/api/problems/**").permitAll() // 문제 관련 API는 인증이 필수가 아님
                        .requestMatchers("/api/submissions/**").authenticated() // 답안 제출 관련 API는 인증 필수
                        .anyRequest().authenticated() // 그 외의 API는 인증 필수
                )
                .oauth2Login(oAuth2LoginConfigurer -> oAuth2LoginConfigurer
                        .userInfoEndpoint(
                                userInfoEndpoint -> userInfoEndpoint.userService(customOAuth2UserService)
                        )
                        .failureHandler(oAuth2LoginFailureHandler)
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT 사용으로 세션 비활성화.
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // 인증되지 않은 사용자가 보호된 리소스에 접근할 때
                        .accessDeniedHandler(customAccessDeniedHandler) // 인증된 사용자가 권한이 없는 리소스에 접근할 때
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // UsernamePasswordAuthenticationFilter 전에 JWT 필터 추가
                .build();

    }

}