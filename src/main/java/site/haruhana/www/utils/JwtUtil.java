package site.haruhana.www.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import site.haruhana.www.dto.TokenDto;
import site.haruhana.www.entity.user.Role;
import site.haruhana.www.entity.user.User;
import site.haruhana.www.repository.UserRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;

    private final String issuer;

    private final String audience;

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 30; // 30분

    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24; // 1일

    private final UserRepository userRepository;

    public JwtUtil(
            @Value("${spring.security.jwt.secret}") String secret,
            @Value("${spring.security.jwt.issuer}") String issuer,
            @Value("${spring.security.jwt.audience}") String audience,
            UserRepository userRepository
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
        this.userRepository = userRepository;
    }

    public TokenDto generateTokens(User user) {
        return generateTokens(user.getId(), user.getRole());
    }

    public TokenDto generateTokens(Long userId, Role userRole) {
        Date now = new Date();
        Date accessTokenExpiry = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);
        Date refreshTokenExpiry = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME);

        return TokenDto.builder()
                .tokenType("Bearer")
                .role(userRole)
                .accessToken(createAccessToken(userId, userRole, accessTokenExpiry))
                .accessTokenExpiresAt(accessTokenExpiry)
                .refreshToken(createRefreshToken(userId, refreshTokenExpiry))
                .refreshTokenExpiresAt(refreshTokenExpiry)
                .build();
    }

    public String createAccessToken(Long userId, Role userRole, Date accessTokenExpiry) {
        return Jwts.builder()
                // Header: 토큰의 타입(JWT)를 담는다. 서명에 사용된 알고리즘의 경우 signWith 메소드에서 자동으로 설정된다.
                .header()
                .add("typ", "JWT") // 토큰의 타입 지정. 여기서는 JWT를 사용.
                .and()

                // Payload: 토큰에 담을 클레임(데이터)을 지정한다. 클레임에는 사용자의 이름, 역할, ID 등이 포함될 수 있다.
                .issuer(issuer) // iss 클레임: 토큰 발급자를 지정
                .subject(String.valueOf(userId)) // sub 클레임: 토큰의 주제. 보통 사용자 ID를 지정
                .audience() // aud 클레임: 토큰 대상자를 지정.
                .add(audience)
                .and()
                .expiration(accessTokenExpiry) // exp 클레임: 토큰 만료 시간을 지정
//                .notBefore(Date(System.currentTimeMillis())) // nbf 클레임: 토큰 활성 날짜를 지정. 헤딩 시간 이전에는 토큰이 활성화되지 않는다.
                .issuedAt(new Date()) // iat 클레임: 토큰 발급 시간을 지정
//                .id(UUID.randomUUID().toString()) // jti 클레임: JWT 토큰 식별자를 지정
                .claim("role", userRole) // 사용자 정의 클레임: 사용자의 역할

                // Signature: header와 payload를 암호화한 결과. 이 부분이 토큰의 무결성을 보장하는 부분
                .signWith(secretKey) // signWith 메소드를 사용해 서명 알고리즘과 키를 지정

                // compact 메소드를 호출해 JWT 토큰 문자열을 생성한다.
                .compact();
    }

    public String createRefreshToken(Long userId, Date refreshTokenExpiry) {
        return Jwts.builder()
                // Header: 토큰의 타입(JWT)를 담는다. 서명에 사용된 알고리즘의 경우 signWith 메소드에서 자동으로 설정된다.
                .header()
                .add("typ", "JWT") // 토큰의 타입 지정. 여기서는 JWT를 사용.
                .and()

                // Payload: 토큰에 담을 클레임(데이터)을 지정한다. Access Token과 달리 Refresh Token에서는 간단한 정보만 포함한다.
                .issuer(issuer) // iss 클레임: 토큰 발급자를 지정
                .subject(String.valueOf(userId)) // sub 클레임: 토큰의 주제. 보통 사용자 ID를 지정
                .audience() // aud 클레임: 토큰 대상자를 지정.
                .add(audience)
                .and()
                .expiration(refreshTokenExpiry) // exp 클레임: 토큰 만료 시간을 지정
                .issuedAt(new Date()) // iat 클레임: 토큰 발급 시간을 지정

                // Signature: header와 payload를 암호화한 결과. 이 부분이 토큰의 무결성을 보장하는 부분
                .signWith(secretKey) // signWith 메소드를 사용해 서명 알고리즘과 키를 지정

                // compact 메소드를 호출해 JWT 토큰 문자열을 생성한다.
                .compact();
    }

    public TokenDto refreshTokens(String refreshToken) {
        try {
            // 토큰 파싱
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            // 만료 시간 검증
            if (claims.getExpiration().before(new Date())) {
                throw new ExpiredJwtException(null, claims, "리프레시 토큰이 만료되었습니다.");
            }

            // 클레임에서 사용자 정보 추출
            Long userId = Long.parseLong(claims.getSubject());

            // 사용자 정보 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new JwtException("사용자 정보를 찾을 수 없습니다."));

            // 새로운 토큰 발급
            TokenDto tokens = generateTokens(user);
            log.info("사용자({})의 리프레시 토큰을 재발급하는데 성공하였습니다.", user.getId());

            return tokens;

        } catch (ExpiredJwtException e) {
            log.warn("만료된 리프레시 토큰으로 재발급을 시도했습니다.");
            throw new JwtException("토큰이 만료되었습니다.");

        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 리프레시 토큰으로 재발급을 시도했습니다.: {}", e.getMessage());
            throw new JwtException("토큰이 유효하지 않습니다.");
        }
    }

    public boolean validateToken(String accessToken) {
        try {
            // 토큰 파싱
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();

            // 토큰 만료 시간 검증
            return !claims.getExpiration().before(new Date());

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String accessToken) {
        try {
            // 토큰 파싱
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();

            // 사용자 ID 추출
            return Long.parseLong(claims.getSubject());

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 토큰으로 사용자 ID를 추출하려고 시도했습니다.: {}", e.getMessage());
            throw new JwtException("토큰이 유효하지 않습니다.");
        }
    }

}