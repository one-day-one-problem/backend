package site.haruhana.www.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // Rest API 사용으로 CSRF 비활성화.
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().permitAll() // Todo: 현재 모든 요청을 허용하고 있지만, 추후 인증 관련 API가 개발되면 수정 필요.
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
                .build();

    }

}