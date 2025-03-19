package site.haruhana.www.dto.submission.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRequestDto {

    /**
     * 문제 ID
     */
    @NotNull(message = "문제 ID는 필수입니다.")
    private Long problemId;

    /**
     * 제출한 답안
     * <ul>
     *     <li>객관식: 선택한 옵션 ID들을 콤마(,)로 구분하여 제출</li>
     *     <li>주관식: 작성한 답안 텍스트</li>
     * </ul>
     */
    @NotBlank(message = "답안은 필수입니다.")
    private String answer;

    /**
     * 문제 풀이에 소요된 시간(초)
     */
    @NotNull(message = "소요 시간은 필수입니다.")
    @Min(value = 1, message = "소요 시간은 1초 이상이어야 합니다.")
    private Integer duration;

}
