package site.haruhana.www.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.haruhana.www.dto.problem.ProblemSortType;
import site.haruhana.www.dto.problem.ProblemSummaryDto;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.repository.ProblemRepository;
import site.haruhana.www.utils.RandomUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

    // 의존성 주입
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
    @Transactional
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

    /**
     * 문제 목록을 조회하는 메서드
     *
     * @param page       페이지 번호
     * @param size       페이지 크기
     * @param category   문제 카테고리
     * @param difficulty 문제 난이도
     * @param sortType   문제 정렬 기준
     * @return {@link Page<ProblemSummaryDto>} 문제 목록
     */
    @Transactional(readOnly = true)
    public Page<ProblemSummaryDto> getProblems(int page, int size, ProblemCategory category, ProblemDifficulty difficulty, ProblemSortType sortType) {
        return problemRepository.findProblemsWithFilters(
                PageRequest.of(page, size, sortType.getSort()),
                category,
                difficulty
        );
    }
}
