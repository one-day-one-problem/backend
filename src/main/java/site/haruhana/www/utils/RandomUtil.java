package site.haruhana.www.utils;

import org.springframework.stereotype.Component;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;

import java.util.Random;

@Component
public class RandomUtil {

    private final Random random = new Random();

    /**
     * 랜덤으로 카테고리를 반환하는 메소드
     *
     * @return 랜덤 카테고리
     */
    public ProblemCategory getRandomCategory() {
        ProblemCategory[] categories = ProblemCategory.values();
        return categories[random.nextInt(categories.length)];
    }

    /**
     * 랜덤으로 난이도를 반환하는 메소드
     *
     * @return 랜덤 난이도
     */
    public ProblemDifficulty getRandomDifficulty() {
        ProblemDifficulty[] difficulties = ProblemDifficulty.values();
        return difficulties[random.nextInt(difficulties.length)];
    }

    /**
     * 랜덤으로 boolean 값을 반환하는 메소드
     *
     * @return 랜덤 boolean 값
     */
    public boolean getRandomBoolean() {
        return random.nextBoolean();
    }
}
