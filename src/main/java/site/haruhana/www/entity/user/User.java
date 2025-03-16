package site.haruhana.www.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.haruhana.www.entity.BaseTimeEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseTimeEntity {

    /**
     * 사용자 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 이름
     */
    @Column(nullable = false)
    private String name;

    /**
     * 사용자 이메일
     */
    @Column(nullable = false)
    private String email;

    /**
     * 사용자 인증 제공자 (구글/카카오/네이버/깃허브)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    /**
     * 사용자 프로필 이미지 URL
     */
    @Column(name = "profile_image_url", nullable = true) // 사용자가 프로필 이미지 정보 수집을 거부할 수 있으므로 nullable = true
    private String profileImageUrl;

    /**
     * 사용자 역할 (USER/ADMIN)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

}