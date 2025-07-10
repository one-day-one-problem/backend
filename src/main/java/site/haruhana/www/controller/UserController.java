package site.haruhana.www.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.haruhana.www.dto.BaseResponse;
import site.haruhana.www.dto.user.UserDto;
import site.haruhana.www.entity.user.User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    
    /**
     * 현재 인증된 사용자의 정보를 조회하는 API
     *
     * @param user 현재 인증된 사용자
     * @return 사용자 정보
     */
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserDto>> getCurrentUser(
            @AuthenticationPrincipal @NotNull User user
    ) {
        UserDto data = UserDto.from(user);
        return ResponseEntity.ok(BaseResponse.onSuccess("현재 사용자 정보를 성공적으로 조회했습니다.", data));
    }
    
}
