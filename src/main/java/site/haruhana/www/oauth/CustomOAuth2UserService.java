package site.haruhana.www.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.haruhana.www.entity.user.User;
import site.haruhana.www.repository.UserRepository;

import java.util.Collections;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            // OAuth2 정보를 가져온다.
            OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

            // OAuth2 서비스에서 가져온 유저 정보의 유효성 검증
            if (oAuth2User == null || oAuth2User.getAttributes().isEmpty()) {
                throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info"), "OAuth2 사용자 정보가 비어있습니다.");
            }

            // OAuth2 로그인을 처리한 서비스 정보를 가져온다. (google, kakao, naver)
            String registrationId = userRequest.getClientRegistration().getRegistrationId();

            // OAuth2 로그인 진행 시 키가 되는 필드값(Primary Key)을 가져온다.
            String userNameAttributeKey = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

            // OAuthAttributes 클래스로 변환한다. (서로 다른 OAuth2 서비스에서 가져온 유저 정보는 응답 데이터가 다르게 구성되어 있기 때문에 통일시킨다.)
            OAuthAttributes attributes = OAuthAttributes.of(registrationId, oAuth2User.getAttributes());

            // OAuth2 서비스에서 가져온 유저 정보의 유효성 검증
            if (attributes.getEmail() == null || attributes.getProvider() == null) {
                throw new OAuth2AuthenticationException(new OAuth2Error("missing_user_info"), "필수 사용자 정보가 없습니다.");
            }

            // 사용자 정보가 DB에 없는 경우 저장한다.
            User user = saveUserIfNotExists(attributes);

            // OAuth2 로그인 사용자 정보를 담는 객체를 생성 및 반환한다.
            return CustomOAuth2User.builder()
                    .authorities(Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())))
                    .attributes(attributes.getAttributes())
                    .nameAttributeKey(userNameAttributeKey)
                    .userId(user.getId())
                    .role(user.getRole())
                    .build();

        } catch (OAuth2AuthenticationException e) {
            log.error("OAuth2 인증 실패: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("OAuth2 처리 중 예상치 못한 오류 발생", e);
            throw new OAuth2AuthenticationException(new OAuth2Error("authentication_error"), "인증 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 정보를 저장하거나 업데이트하는 메소드.
     * <p>
     * 이미 존재하는 회원이면 기존 정보를 유지하고,
     * 처음 가입하는 회원이면 User 테이블을 생성한다.
     * </p>
     *
     * @param authAttributes OAuth2 로그인 시 가져온 유저 정보
     */
    private User saveUserIfNotExists(OAuthAttributes authAttributes) {
        Objects.requireNonNull(authAttributes.getEmail(), "이메일은 null일 수 없습니다.");
        Objects.requireNonNull(authAttributes.getProvider(), "제공자 정보는 null일 수 없습니다.");

        return userRepository.findByProviderAndEmail(authAttributes.getProvider(), authAttributes.getEmail())
                .orElseGet(() -> {
                    log.info("새로운 사용자를 등록합니다. email: {}", authAttributes.getEmail());
                    return userRepository.save(authAttributes.toEntity());
                });
    }
}
