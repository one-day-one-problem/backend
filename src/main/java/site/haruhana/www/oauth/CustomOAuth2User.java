package site.haruhana.www.oauth;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import site.haruhana.www.entity.user.Role;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {
    private Long userId; // 사용자 식별자
    private Role role; // 사용자 역할

    @Builder
    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes, String nameAttributeKey,
                            Long userId, Role role) {

        super(authorities, attributes, nameAttributeKey);
        this.userId = userId;
        this.role = role;
    }
}