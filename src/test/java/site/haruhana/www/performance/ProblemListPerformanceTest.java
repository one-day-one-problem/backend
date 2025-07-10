package site.haruhana.www.performance;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import site.haruhana.www.controller.ProblemController;
import site.haruhana.www.dto.BaseResponse;
import site.haruhana.www.dto.problem.ProblemPage;
import site.haruhana.www.dto.problem.ProblemSortType;
import site.haruhana.www.dto.problem.ProblemSummaryDto;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.ProblemType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("문제 목록 조회 성능 테스트")
class ProblemListPerformanceTest {

    @Autowired
    private ProblemController problemController;

    private Random random;

    private static final int TOTAL_PROBLEMS = 115021; // 실제 데이터 개수

    @BeforeEach
    void setUp() {
        random = new Random();
        log.info("성능 테스트 준비 완료");
    }

    @Test
    @DisplayName("비인증 사용자 - Controller 직접 호출 - 10000회 요청")
    @Transactional(readOnly = true)
    void measureUnauthenticatedUserControllerPerformanceWith10000Requests() {
        // Given
        final int totalRequests = 10000;
        List<Long> responseTimes = new ArrayList<>();

        log.info("=== 비인증 사용자 Controller 직접 호출 성능 테스트 시작 ===");
        log.info("총 요청 횟수: {} 회", totalRequests);

        long totalStartTime = System.nanoTime();

        // When & Then
        for (int i = 0; i < totalRequests; i++) {
            // 랜덤 파라미터 생성 (비인증 사용자용)
            TestParams params = generateRandomTestParamsForUnauthenticated();

            // 개별 요청 시간 측정 (Controller 직접 호출, 비인증 사용자)
            long startTime = System.nanoTime();

            // ProblemController 직접 호출 (사용자 정보 null로 전달)
            BaseResponse<ProblemPage<ProblemSummaryDto>> response = problemController.getProblems(
                    params.page,
                    params.size,
                    params.category,
                    params.difficulty,
                    params.type,
                    params.sortType,
                    false,  // 비인증 사용자는 미해결 문제 필터 사용 불가
                    null    // 비인증 사용자 (AuthenticationPrincipal이 null)
            ).getBody();

            long endTime = System.nanoTime();
            long responseTime = endTime - startTime;
            responseTimes.add(responseTime);

            // 결과 검증
            assertThat(response).isNotNull();
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().getProblems()).isNotNull();

            // 1000회마다 중간 진행상황 로깅
            if ((i + 1) % 1000 == 0) {
                double avgTimeMs = responseTimes.stream()
                        .skip(Math.max(0, responseTimes.size() - 1000))
                        .mapToLong(Long::longValue)
                        .average()
                        .orElse(0.0) / 1_000_000.0;

                log.info("진행 상황: {}/{} 완료, 최근 1000회 평균 응답시간: {}ms", i + 1, totalRequests, String.format("%.2f", avgTimeMs));
            }
        }

        long totalEndTime = System.nanoTime();

        // 성능 통계 계산 및 출력
        log.info("=== 비인증 사용자 Controller 성능 테스트 결과 ===");
        logPerformanceStatistics(responseTimes, totalStartTime, totalEndTime, totalRequests);
    }

    /**
     * 비인증 사용자를 위한 랜덤 테스트 파라미터 생성
     * (미해결 문제 필터 사용 불가)
     * 115,021개 데이터를 고려한 실제 규모 반영
     */
    private TestParams generateRandomTestParamsForUnauthenticated() {
        TestParams params = new TestParams();

        // 페이지 크기를 실제 UI에서 사용하는 값들로 설정 (12, 24, 36)
        int[] pageSizes = {12, 24, 36};
        params.size = pageSizes[random.nextInt(pageSizes.length)];

        // 실제 데이터 개수에 따라 최대 페이지 번호 계산
        int maxPage = calculateMaxPage(TOTAL_PROBLEMS, params.size);
        params.page = random.nextInt(maxPage + 1); // 0부터 maxPage까지

        // 실제 존재하는 카테고리들을 순환하며 사용
        params.category = getRandomCategory();

        // 실제 존재하는 난이도들을 순환하며 사용
        params.difficulty = getRandomDifficulty();

        // 실제 존재하는 타입들을 순환하며 사용
        params.type = getRandomType();

        // 모든 정렬 타입을 골고루 사용
        params.sortType = getRandomSortType();

        // 비인증 사용자는 미해결 문제 필터 사용 불가
        params.onlyUnsolved = false;

        // 비인증 사용자
        params.useAuth = false;

        return params;
    }

    /**
     * 주어진 데이터 개수와 페이지 크기에 따른 최대 페이지 번호 계산
     *
     * @param totalData 총 데이터 개수
     * @param pageSize  페이지 크기
     * @return 최대 페이지 번호 (0-based)
     */
    private int calculateMaxPage(int totalData, int pageSize) {
        return (totalData - 1) / pageSize; // 0-based 페이지 번호의 최대값
    }

    private ProblemCategory getRandomCategory() {
        ProblemCategory[] categories = ProblemCategory.values();
        return categories[random.nextInt(categories.length)];
    }

    private ProblemDifficulty getRandomDifficulty() {
        ProblemDifficulty[] difficulties = ProblemDifficulty.values();
        return difficulties[random.nextInt(difficulties.length)];
    }

    private ProblemType getRandomType() {
        ProblemType[] types = ProblemType.values();
        return types[random.nextInt(types.length)];
    }

    private ProblemSortType getRandomSortType() {
        ProblemSortType[] sortTypes = ProblemSortType.values();
        return sortTypes[random.nextInt(sortTypes.length)];
    }

    private void logPerformanceStatistics(List<Long> responseTimes, long totalStartTime, long totalEndTime, int totalRequests) {
        // 응답시간 통계 계산 (nanoseconds to milliseconds)
        double avgTimeMs = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0) / 1_000_000.0;

        double minTimeMs = responseTimes.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0L) / 1_000_000.0;

        double maxTimeMs = responseTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) / 1_000_000.0;

        // 중간값 계산
        List<Long> sortedTimes = responseTimes.stream().sorted().toList();
        double medianTimeMs = sortedTimes.get(sortedTimes.size() / 2) / 1_000_000.0;

        // 95 퍼센타일 계산
        double p95TimeMs = sortedTimes.get((int) (sortedTimes.size() * 0.95)) / 1_000_000.0;

        // 99 퍼센타일 계산
        double p99TimeMs = sortedTimes.get((int) (sortedTimes.size() * 0.99)) / 1_000_000.0;

        // 전체 소요시간
        double totalTimeMs = (totalEndTime - totalStartTime) / 1_000_000.0;
        double totalTimeSeconds = totalTimeMs / 1000.0;

        // TPS (Transactions Per Second) 계산
        double tps = totalRequests / totalTimeSeconds;

        // 결과 출력
        log.info("=== 성능 테스트 결과 ===");
        log.info("총 요청 수: {} 회", totalRequests);
        log.info("총 소요시간: {}ms ({}초)", String.format("%.2f", totalTimeMs), String.format("%.2f", totalTimeSeconds));
        log.info("평균 응답시간: {}ms", String.format("%.2f", avgTimeMs));
        log.info("중간값 응답시간: {}ms", String.format("%.2f", medianTimeMs));
        log.info("최소 응답시간: {}ms", String.format("%.2f", minTimeMs));
        log.info("최대 응답시간: {}ms", String.format("%.2f", maxTimeMs));
        log.info("95 퍼센타일: {}ms", String.format("%.2f", p95TimeMs));
        log.info("99 퍼센타일: {}ms", String.format("%.2f", p99TimeMs));
        log.info("TPS (처리량): {} requests/sec", String.format("%.2f", tps));
        log.info("=== 테스트 완료 ===");
    }

    private static class TestParams {
        int page;
        int size;
        ProblemCategory category;
        ProblemDifficulty difficulty;
        ProblemType type;
        ProblemSortType sortType;
        boolean onlyUnsolved;
        boolean useAuth;
    }
}
