package site.haruhana.www.service;

import site.haruhana.www.entity.problem.Category;
import site.haruhana.www.entity.problem.Difficulty;
import site.haruhana.www.entity.problem.Problem;

public interface AIService {

    /**
     * AI를 사용하여 객관식 문제를 생성하는 메소드
     *
     * @param category   생성할 문제의 카테고리
     * @param difficulty 생성할 문제의 난이도
     * @return 카테고리와 난이도에 맞게 AI가 생성한 객관식 문제
     */
    Problem generateMultipleChoiceQuestion(Category category, Difficulty difficulty);

    /**
     * AI를 사용하여 주관식 문제를 생성하는 메소드
     *
     * @param category   생성할 문제의 카테고리
     * @param difficulty 생성할 문제의 난이도
     * @return 카테고리와 난이도에 맞게 AI가 생성한 주관식 문제
     */
    Problem generateSubjectiveQuestion(Category category, Difficulty difficulty);

}