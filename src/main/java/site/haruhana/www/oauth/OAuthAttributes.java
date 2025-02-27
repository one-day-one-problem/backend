package site.haruhana.www.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import site.haruhana.www.entity.AuthProvider;
import site.haruhana.www.entity.Role;
import site.haruhana.www.entity.User;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class OAuthAttributes {

    private Map<String, Object> attributes;      // 소셜 로그인 시 반환되는 JSON 형태의 사용자 정보
    private AuthProvider provider;               // 소셜 서비스 제공자 (GOOGLE, KAKAO, NAVER)
    private String name;                         // 사용자 이름
    private String email;                        // 사용자 이메일
    private String profileImageUrl;              // 사용자 프로필 이미지 URL

    /**
     * 소셜 로그인 제공자에 따른 OAuthAttributes 객체 생성하는 정적 팩토리 메서드
     *
     * @param registrationId 소셜 서비스 제공자 ID (google, kakao 등)
     * @param attributes     소셜 로그인 시 반환되는 JSON 형태의 사용자 정보
     * @return 소셜 로그인 제공자에 맞는 OAuthAttributes 객체
     */
    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException("OAuth2 attributes cannot be null");
        }

        return switch (registrationId) {
            case "google" -> ofGoogle(attributes);
            case "kakao" -> ofKakao(attributes);
            case "naver" -> ofNaver(attributes);
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        };
    }

    /**
     * Google OAuth2 인증 결과를 기반으로 OAuthAttributes 객체 생성하는 정적 팩토리 메서드
     *
     * @param attributes Google에서 반환한 JSON 형태의 사용자 정보
     * @return Google 사용자 정보가 담긴 OAuthAttributes 객체
     */
    private static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .attributes(attributes)
                .provider(AuthProvider.GOOGLE)
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .profileImageUrl((String) attributes.get("picture"))
                .build();
    }

    /**
     * Kakao OAuth2 인증 결과를 기반으로 OAuthAttributes 객체 생성하는 정적 팩토리 메서드
     *
     * @param attributes Kakao에서 반환한 JSON 형태의 사용자 정보
     * @return Kakao 사용자 정보가 담긴 OAuthAttributes 객체
     */
    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            throw new IllegalArgumentException("Kakao account info is missing");
        }

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null) {
            throw new IllegalArgumentException("Kakao profile info is missing");
        }

        return OAuthAttributes.builder()
                .attributes(attributes)
                .provider(AuthProvider.KAKAO)
                .name((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .profileImageUrl((String) profile.get("profile_image_url"))
                .build();
    }

    /**
     * Naver OAuth2 인증 결과를 기반으로 OAuthAttributes 객체 생성하는 정적 팩토리 메서드
     *
     * @param attributes Naver에서 반환한 JSON 형태의 사용자 정보
     * @return Naver 사용자 정보가 담긴 OAuthAttributes 객체
     */
    private static OAuthAttributes ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            throw new IllegalArgumentException("Naver response info is missing");
        }

        return OAuthAttributes.builder()
                .attributes(attributes)
                .provider(AuthProvider.NAVER)
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .profileImageUrl((String) response.get("profile_image"))
                .build();
    }

    /**
     * OAuthAttributes 정보를 기반으로 User 엔티티 생성하는 메서드
     * <p>
     * 소셜 로그인 사용자의 경우 이메일을 임시 비밀번호로 사용하며,
     * 기본 권한은 USER로 설정됨
     *
     * @return 생성된 User 엔티티
     */
    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .provider(provider)
                .profileImageUrl(profileImageUrl)
                .role(Role.USER)
                .build();
    }

}