package site.haruhana.www.utils;

import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;

import java.util.Random;

public final class RandomUtil {

    private static final Random RANDOM = new Random();

    /**
     * 인스턴스 생성 방지용 private 생성자
     */
    private RandomUtil() {

    }

    /**
     * 랜덤으로 카테고리를 반환하는 메소드
     *
     * @return 랜덤 카테고리
     */
    public static ProblemCategory getRandomCategory() {
        ProblemCategory[] categories = ProblemCategory.values();
        return categories[RANDOM.nextInt(categories.length)];
    }

    /**
     * 랜덤으로 난이도를 반환하는 메소드
     *
     * @return 랜덤 난이도
     */
    public static ProblemDifficulty getRandomDifficulty() {
        ProblemDifficulty[] difficulties = ProblemDifficulty.values();
        return difficulties[RANDOM.nextInt(difficulties.length)];
    }

    /**
     * 랜덤으로 boolean 값을 반환하는 메소드
     *
     * @return 랜덤 boolean 값
     */
    public static boolean getRandomBoolean() {
        return RANDOM.nextBoolean();
    }
}
