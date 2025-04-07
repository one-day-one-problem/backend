package site.haruhana.www.dto.submission.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.ProblemType;

import java.time.LocalDateTime;

/**
 * 사용자의 문제 풀이 기록 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값은 JSON 응답에서 제외
public class SubmissionHistoryResponseDto {
    /**
     * 문제 ID
     */
    private Long id;

    /**
     * 문제 제목
     */
    private String title;

    /**
     * 문제 카테고리
     */
    private ProblemCategory category;

    /**
     * 문제 난이도
     */
    private ProblemDifficulty difficulty;

    /**
     * 문제 유형 (객관식/주관식)
     */
    private ProblemType type;

    /**
     * 문제 해결 시간
     */
    private LocalDateTime solvedAt;

    /**
     * 문제 해결 여부
     */
    private Boolean isCorrect;

    /**
     * 주관식 문제 점수 (객관식인 경우 null)
     */
    private Double score;

}
