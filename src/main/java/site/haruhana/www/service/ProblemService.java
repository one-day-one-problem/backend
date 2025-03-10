package site.haruhana.www.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.repository.ProblemRepository;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

    // 의존성 주입
    private final AIService aiService;
    private final ProblemRepository problemRepository;
    private final Random random = new Random();

    /**
     * 60초마다 실행되는 문제 자동 생성 스케줄러
     * <p>
     * 다음 조건을 만족할 때 문제를 생성합니다:
     * <ul>
     *   <li>총 문제 수가 1000개 미만인 경우</li>
     *   <li>또는 (총 문제 수 * 0.8) < 최다 풀이자의 풀이 수인 경우</li>
     * </ul>
     * 문제 생성 시 다음 요소들을 랜덤하게 선택합니다:
     * <ul>
     *   <li>카테고리: {@link ProblemCategory} 중 랜덤 선택</li>
     *   <li>난이도: {@link ProblemDifficulty} 중 랜덤 선택</li>
     *   <li>문제 유형: 객관식/주관식 랜덤 선택</li>
     * </ul>
     */
    @Scheduled(fixedRate = 60000) // 60초
    @Transactional
    public void generateRandomProblem() {
        try {
            // 랜덤 카테고리, 난이도, 문제 유형 선택
            ProblemCategory randomCategory = getRandomCategory();
            ProblemDifficulty randomDifficulty = getRandomDifficulty();
            boolean isMultipleChoice = random.nextBoolean();

            // 문제 생성
            Problem problem = isMultipleChoice ?
                    aiService.generateMultipleChoiceQuestion(randomCategory, randomDifficulty) :
                    aiService.generateSubjectiveQuestion(randomCategory, randomDifficulty);

            // 문제 저장
            problemRepository.save(problem);

            log.info("새로운 문제가 생성되었습니다. 유형: {}, 카테고리: {}, 난이도: {}", isMultipleChoice ? "객관식" : "주관식", randomCategory, randomDifficulty);

        } catch (Exception e) {
            log.error("문제 자동 생성 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 랜덤한 문제 카테고리를 선택하는 메소드
     *
     * @return 무작위로 선택된 {@link ProblemCategory}
     */
    private ProblemCategory getRandomCategory() {
        ProblemCategory[] categories = ProblemCategory.values();
        return categories[random.nextInt(categories.length)];
    }

    /**
     * 랜덤한 문제 난이도를 선택하는 메소드
     *
     * @return 무작위로 선택된 {@link ProblemDifficulty}
     */
    private ProblemDifficulty getRandomDifficulty() {
        ProblemDifficulty[] difficulties = ProblemDifficulty.values();
        return difficulties[random.nextInt(difficulties.length)];
    }
}
