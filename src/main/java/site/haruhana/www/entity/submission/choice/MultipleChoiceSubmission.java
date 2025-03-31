package site.haruhana.www.entity.submission.choice;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import site.haruhana.www.entity.submission.Submission;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@DiscriminatorValue("MULTIPLE_CHOICE")
public class MultipleChoiceSubmission extends Submission {

    /**
     * 정답 여부를 업데이트하는 메서드
     *
     * @param isCorrect 정답 여부
     */
    public void updateGradingResult(boolean isCorrect) {
        super.setIsCorrect(isCorrect);
    }
}
