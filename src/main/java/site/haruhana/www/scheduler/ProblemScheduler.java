package site.haruhana.www.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.repository.ProblemRepository;
import site.haruhana.www.service.AIService;
import site.haruhana.www.utils.RandomUtil;

/**
 * 문제 생성을 위한 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemScheduler {

    private final AIService aiService;

    private final ProblemRepository problemRepository;

    /**
     * 60초마다 실행되는 문제 자동 생성 스케줄러
     * <p>
     * 문제 생성 시 다음 요소들을 랜덤하게 선택합니다:
     * <ul>
     *   <li>카테고리: {@link ProblemCategory} 중 랜덤 선택</li>
     *   <li>난이도: {@link ProblemDifficulty} 중 랜덤 선택</li>
     *   <li>문제 유형: 객관식/주관식 랜덤 선택</li>
     * </ul>
     */
    @Scheduled(fixedRate = 60000) // 60초
    public void generateRandomProblem() {
        try {
            // 랜덤 카테고리, 난이도, 문제 유형 선택
            var randomCategory = RandomUtil.getRandomCategory();
            var randomDifficulty = RandomUtil.getRandomDifficulty();
            var problemType = RandomUtil.getRandomProblemType();

            // 문제 생성
            Problem problem = switch (problemType) {
                case MULTIPLE_CHOICE -> aiService.generateMultipleChoiceQuestion(randomCategory, randomDifficulty);
                case SUBJECTIVE -> aiService.generateSubjectiveQuestion(randomCategory, randomDifficulty);
            };

            // 문제 저장
            problemRepository.save(problem);

            log.info("새로운 문제가 생성되었습니다. 유형: {}, 카테고리: {}, 난이도: {}", problemType.getDescription(), randomCategory, randomDifficulty);

        } catch (Exception e) {
            log.error("문제 자동 생성 중 오류 발생: {}", e.getMessage());
        }
    }

}
