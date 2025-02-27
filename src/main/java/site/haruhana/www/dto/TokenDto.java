package site.haruhana.www.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import site.haruhana.www.entity.Role;

import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
public class TokenDto {

    private String tokenType;

    private Role role;

    private String accessToken;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date accessTokenExpiresAt;

    private String refreshToken;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date refreshTokenExpiresAt;
}