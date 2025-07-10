package site.haruhana.www.dto.user;

import lombok.Builder;
import lombok.Getter;
import site.haruhana.www.entity.user.AuthProvider;
import site.haruhana.www.entity.user.User;

@Getter
@Builder
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String profileImageUrl;
    private AuthProvider provider;

    /**
     * User 엔티티를 UserResponseDto로 변환하는 정적 팩토리 메소드
     *
     * @param user 변환할 User 엔티티
     * @return 변환된 UserResponseDto 객체
     */
    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .provider(user.getProvider())
                .build();
    }
}
