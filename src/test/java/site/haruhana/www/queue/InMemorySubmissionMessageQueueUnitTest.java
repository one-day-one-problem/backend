package site.haruhana.www.queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.ProblemProvider;
import site.haruhana.www.queue.impl.InMemorySubmissionMessageQueue;
import site.haruhana.www.queue.message.GradingData;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;

class InMemorySubmissionMessageQueueUnitTest {

    private InMemorySubmissionMessageQueue messageQueue;

    private Problem testProblem;

    @BeforeEach
    void setUp() {
        messageQueue = new InMemorySubmissionMessageQueue();

        testProblem = Problem.subjectiveProblemBuilder()
                .title("Test Problem")
                .question("Test Question")
                .category(ProblemCategory.KUBERNETES)
                .difficulty(ProblemDifficulty.MEDIUM)
                .provider(ProblemProvider.AI)
                .build();
    }

    private GradingData createGradingData(Long id) {
        return GradingData.builder()
                .submissionId(id)
                .problemId(testProblem.getId())
                .problemTitle(testProblem.getTitle())
                .problemQuestion(testProblem.getQuestion())
                .gradingCriteria(Collections.singletonList("Test criteria"))
                .sampleAnswer("Sample answer")
                .submittedAnswer("Test Answer")
                .build();
    }

    @Nested
    @DisplayName("기본 큐 작업")
    class BasicQueueOperations {

        @Test
        @DisplayName("큐가 비어있을 때 크기는 0이어야 한다")
        void queueShouldBeEmptyInitially() {
            // given: 새로 생성된 큐가 주어졌을 때
            // when: 큐의 상태를 확인하면
            // then: 큐가 비어있고 크기가 0이어야 한다
            assertAll(
                    "빈 큐 검증",
                    () -> assertThat(messageQueue.isEmpty()).isTrue(),
                    () -> assertThat(messageQueue.size()).isZero()
            );
        }

        @Test
        @DisplayName("큐에 제출물을 추가하면 사이즈가 증가한다")
        void shouldIncreaseSize() {
            // given: 비어있는 큐가 주어졌을 때

            // when: 채점 데이터를 큐에 추가하면
            messageQueue.enqueue(createGradingData(1L));

            // then: 큐의 사이즈가 증가하고 비어있지 않아야 한다
            assertAll(
                    "큐 크기 검증",
                    () -> assertThat(messageQueue.size()).isEqualTo(1),
                    () -> assertThat(messageQueue.isEmpty()).isFalse()
            );
        }

        @Test
        @DisplayName("큐가 비어있을 때 dequeue는 null을 반환한다")
        void dequeueReturnsNullWhenQueueIsEmpty() throws InterruptedException {
            // given: 비어있는 큐가 주어졌을 때

            // when: dequeue를 수행하면
            GradingData result = messageQueue.dequeue();

            // then: null이 반환된다
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("일반 우선순위로 등록된 항목들은 FIFO 순서로 처리된다")
        void normalPriorityItemsAreProcessedInFifoOrder() {
            // given: 세 개의 일반 우선순위 채점 데이터가 있을 때
            GradingData first = createGradingData(1L);
            GradingData second = createGradingData(2L);
            GradingData third = createGradingData(3L);

            // when: 일반 우선순위로 순서대로 큐에 추가하면
            messageQueue.enqueue(first);
            messageQueue.enqueue(second);
            messageQueue.enqueue(third);

            // then: FIFO 순서대로 처리된다
            assertAll(
                    "일반 우선순위 FIFO 순서 검증",
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(1L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(2L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(3L)
            );
        }

        @Test
        @DisplayName("큐가 비어있는지 올바르게 확인한다")
        void isEmptyReturnsCorrectValue() {
            // given: 비어있는 큐가 주어졌을 때

            // when: 하나의 항목을 추가하면
            messageQueue.enqueue(createGradingData(1L));

            // then: 큐가 비어있지 않다고 반환한다
            assertThat(messageQueue.isEmpty()).isFalse();

            // when: 모든 항목을 제거하면
            try {
                messageQueue.dequeue();
            } catch (InterruptedException e) {
                fail("Should not throw exception");
            }

            // then: 큐가 비어있다고 반환한다
            assertThat(messageQueue.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("큐의 사이즈가 올바르게 보고된다")
        void sizeReturnsCorrectValue() throws InterruptedException {
            // given: 비어있는 큐가 주어졌을 때
            assertThat(messageQueue.size()).isEqualTo(0);

            // when: 여러 항목을 추가하면
            messageQueue.enqueue(createGradingData(1L));
            messageQueue.enqueue(createGradingData(2L));
            messageQueue.prioritize(createGradingData(3L));

            // then: 큐의 크기가 정확하게 반영된다
            assertThat(messageQueue.size()).isEqualTo(3);

            // when: 항목을 하나 제거하면
            messageQueue.dequeue();

            // then: 큐의 크기가 감소한다
            assertThat(messageQueue.size()).isEqualTo(2);

            // when: 모든 항목을 제거하면
            messageQueue.dequeue();
            messageQueue.dequeue();

            // then: 큐가 비어있음을 나타낸다
            assertThat(messageQueue.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("우선순위 큐 동작")
    class PriorityQueueOperations {

        @Test
        @DisplayName("우선순위가 높은 항목이 일반 항목보다 먼저 처리되어야 한다")
        void shouldProcessPriorityItemsFirst() {
            // given: 20개의 일반 항목과 5개의 우선순위 항목이 주어졌을 때
            List<Long> normalIds = new ArrayList<>();
            List<Long> priorityIds = new ArrayList<>();

            for (int i = 0; i < 20; i++) {
                messageQueue.enqueue(createGradingData((long) i));
                normalIds.add((long) i);
            }

            for (int i = 100; i < 105; i++) {
                messageQueue.prioritize(createGradingData((long) i));
                priorityIds.add((long) i);
            }

            // when & then: 항목을 처리하면 우선순위 항목이 먼저 처리되고 그 다음 일반 항목이 처리된다
            assertAll(
                    "우선순위 처리 순서 검증",
                    () -> {
                        // 우선순위 항목이 먼저 처리되는지 확인
                        for (int i = 0; i < 5; i++) {
                            assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(priorityIds.get(i));
                        }
                    },
                    () -> {
                        // 그 다음 일반 항목이 처리되는지 확인
                        for (int i = 0; i < 20; i++) {
                            assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(normalIds.get(i));
                        }
                    }
            );
        }

        @Test
        @DisplayName("prioritize 메소드는 항목을 큐의 맨 앞에 추가한다")
        void prioritizeAddsItemToFrontOfQueue() {
            // given: 두 개의 일반 우선순위 항목이 큐에 있을 때
            GradingData normal1 = createGradingData(1L);
            GradingData normal2 = createGradingData(2L);
            messageQueue.enqueue(normal1);
            messageQueue.enqueue(normal2);

            // when: 높은 우선순위로 새로운 항목을 추가하면
            GradingData highPriority = createGradingData(3L);
            messageQueue.prioritize(highPriority);

            // then: 우선순위가 높은 항목이 가장 먼저 처리된다
            assertAll(
                    "우선순위 항목 처리 순서 검증",
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(3L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(1L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(2L)
            );
        }

        @Test
        @DisplayName("여러 높은 우선순위 항목들은 추가된 순서대로 처리된다 (FIFO)")
        void multiplePriorityItemsAreProcessedInFifoOrder() {
            // given: 세 개의 높은 우선순위 채점 데이터가 주어졌을 때
            GradingData highPriority1 = createGradingData(1L);
            GradingData highPriority2 = createGradingData(2L);
            GradingData highPriority3 = createGradingData(3L);

            // when: 순서대로 우선순위 큐에 추가하면
            messageQueue.prioritize(highPriority1);
            messageQueue.prioritize(highPriority2);
            messageQueue.prioritize(highPriority3);

            // then: 추가된 순서대로 처리된다 (FIFO)
            assertAll(
                    "높은 우선순위 항목 FIFO 검증",
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(1L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(2L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(3L)
            );
        }

        @Test
        @DisplayName("일반 항목과 우선순위 항목이 섞여있을 때 우선순위 그룹 내에서 FIFO로 처리되고, 그 다음 일반 항목이 처리된다")
        void mixedPriorityAndNormalItemsAreProcessedInOrder() {
            // given: 일반 항목과 우선순위 항목이 섞여있는 상태가 주어졌을 때
            GradingData normal1 = createGradingData(1L);
            GradingData normal2 = createGradingData(2L);
            GradingData highPriority1 = createGradingData(3L);
            GradingData normal3 = createGradingData(4L);
            GradingData highPriority2 = createGradingData(5L);

            // when: 순서대로 큐에 추가하면
            messageQueue.enqueue(normal1);
            messageQueue.prioritize(highPriority1);
            messageQueue.enqueue(normal2);
            messageQueue.enqueue(normal3);
            messageQueue.prioritize(highPriority2);

            // then: 우선순위 그룹이 먼저 FIFO로 처리되고, 그 다음 일반 항목이 FIFO로 처리된다
            assertAll(
                    "혼합 항목 처리 순서 검증",
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(3L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(5L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(1L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(2L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(4L)
            );
        }

        @Test
        @DisplayName("우선순위 큐는 우선순위 그룹별로 FIFO를 보장한다")
        void queueMaintainsFifoOrderWithinPriorityGroups() {
            // given: 각 우선순위 그룹별로 여러 항목이 주어졌을 때
            GradingData highPriority1 = createGradingData(1L);
            GradingData highPriority2 = createGradingData(2L);
            GradingData normal1 = createGradingData(3L);
            GradingData normal2 = createGradingData(4L);
            GradingData highPriority3 = createGradingData(5L);
            GradingData normal3 = createGradingData(6L);

            // when: 다양한 순서로 큐에 추가하면
            messageQueue.prioritize(highPriority1);
            messageQueue.enqueue(normal1);
            messageQueue.prioritize(highPriority2);
            messageQueue.enqueue(normal2);
            messageQueue.prioritize(highPriority3);
            messageQueue.enqueue(normal3);

            // then: 각 우선순위 그룹 내에서 FIFO 순서가 보장된다
            assertAll(
                    "우선순위 그룹별 FIFO 순서 검증",
                    // 우선순위 그룹 먼저 처리 (FIFO)
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(1L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(2L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(5L),
                    // 그 다음 일반 그룹 처리 (FIFO)
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(3L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(4L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(6L)
            );
        }

        @Test
        @DisplayName("일반 항목과 우선순위 항목이 섞여있을 때 우선순위 항목이 먼저 처리된다")
        void priorityItemsAreProcessedBeforeNormalItems() {
            // given: 일반 항목과 우선순위 항목이 섞여있는 상태가 주어졌을 때
            GradingData normal1 = createGradingData(1L);
            GradingData normal2 = createGradingData(2L);
            GradingData highPriority1 = createGradingData(3L);
            GradingData normal3 = createGradingData(4L);
            GradingData highPriority2 = createGradingData(5L);

            // when: 섞인 순서로 큐에 추가하면
            messageQueue.enqueue(normal1);
            messageQueue.prioritize(highPriority1);
            messageQueue.enqueue(normal2);
            messageQueue.enqueue(normal3);
            messageQueue.prioritize(highPriority2);

            // then: 우선순위 항목이 먼저 처리되고, 그 다음 일반 항목이 처리된다
            assertAll(
                    "우선순위 처리 순서 검증",
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(3L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(5L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(1L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(2L),
                    () -> assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(4L)
            );
        }
    }

    @Nested
    @DisplayName("대용량 데이터 처리")
    class LargeScaleOperations {

        @Test
        @DisplayName("대량의 데이터를 처리할 때도 순서가 보장되어야 한다")
        void shouldMaintainOrderWithLargeDataSet() {
            // given: 1000개의 일반 항목과 100개의 우선순위 항목이 주어졌을 때
            List<Long> expectedOrder = new ArrayList<>();

            // 100개의 우선순위 항목 추가
            for (int i = 0; i < 100; i++) {
                messageQueue.prioritize(createGradingData((long) i + 1000));
                expectedOrder.add((long) i + 1000);
            }

            // 1000개의 일반 항목 추가
            for (int i = 0; i < 1000; i++) {
                messageQueue.enqueue(createGradingData((long) i));
                expectedOrder.add((long) i);
            }

            // when & then: 항목을 처리하면 우선순위 순서대로 처리된다
            assertAll(
                    "대용량 데이터 처리 순서 검증",
                    () -> {
                        // 우선순위 항목 검증 (처음 100개)
                        for (int i = 0; i < 100; i++) {
                            assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(expectedOrder.get(i));
                        }
                    },
                    () -> {
                        // 일반 항목 검증 (나머지 1000개)
                        for (int i = 100; i < 1100; i++) {
                            assertThat(messageQueue.dequeue().getSubmissionId()).isEqualTo(expectedOrder.get(i));
                        }
                    },
                    () -> assertThat(messageQueue.isEmpty()).isTrue()
            );
        }
    }

    @Nested
    @DisplayName("동시성 테스트")
    class ConcurrencyTests {
        @Test
        @DisplayName("여러 스레드에서 동시에 접근해도 데이터 무결성이 보장되어야 한다")
        void shouldMaintainDataIntegrityUnderConcurrentAccess() throws Exception {
            // given: 여러 스레드와 항목이 주어졌을 때
            int producerThreads = 10;
            int itemsPerThread = 1000;
            CountDownLatch latch = new CountDownLatch(producerThreads);
            ExecutorService executor = Executors.newFixedThreadPool(producerThreads);
            Set<Long> processedIds = Collections.synchronizedSet(new HashSet<>());

            // when: 여러 스레드에서 동시에 항목을 추가하면
            for (int i = 0; i < producerThreads; i++) {
                int finalI = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < itemsPerThread; j++) {
                            long id = finalI * itemsPerThread + j;
                            if (j % 5 == 0) { // 20%는 우선순위 항목으로 처리
                                messageQueue.prioritize(createGradingData(id));
                            } else {
                                messageQueue.enqueue(createGradingData(id));
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            // then: 모든 항목이 정상적으로 큐에 추가된다
            assertAll(
                    "동시성 처리 결과 검증",
                    () -> assertThat(messageQueue.size()).isEqualTo(producerThreads * itemsPerThread),
                    () -> {
                        while (!messageQueue.isEmpty()) {
                            GradingData data = messageQueue.dequeue();
                            processedIds.add(data.getSubmissionId());
                        }
                        assertThat(processedIds).hasSize(producerThreads * itemsPerThread);
                    }
            );
        }

        @Test
        @DisplayName("멀티스레드 환경에서 동시에 여러 항목을 추가해도 안전하게 동작한다")
        void threadSafeEnqueuing() throws Exception {
            // given: 여러 스레드와 각 스레드별 작업 항목이 주어졌을 때
            int threadCount = 10;
            int itemsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            // when: 여러 스레드에서 동시에 항목을 추가하면
            for (int i = 0; i < threadCount; i++) {
                int finalI = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < itemsPerThread; j++) {
                            messageQueue.enqueue(createGradingData((long) (finalI * itemsPerThread + j)));
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            // then: 모든 항목이 안전하게 추가된다
            assertThat(messageQueue.size()).isEqualTo(threadCount * itemsPerThread);
        }

        @Test
        @DisplayName("멀티스레드 환경에서 동시에 dequeue해도 각 항목은 한 번만 처리된다")
        void threadSafeDequeuing() throws Exception {
            // given: 여러 항목이 큐에 저장되어 있고 여러 스레드가 주어졌을 때
            int itemCount = 1000;
            for (int i = 0; i < itemCount; i++) {
                messageQueue.enqueue(createGradingData((long) i));
            }

            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<GradingData> processedItems = Collections.synchronizedList(new ArrayList<>());

            // when: 여러 스레드에서 동시에 dequeue를 수행하면
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        while (true) {
                            GradingData data = messageQueue.dequeue();
                            if (data == null) break;
                            processedItems.add(data);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            // then: 모든 항목이 정확히 한 번씩만 처리된다
            assertAll(
                    "병렬 dequeue 결과 검증",
                    () -> assertThat(processedItems).hasSize(itemCount),
                    () -> assertThat(messageQueue.isEmpty()).isTrue(),
                    () -> {
                        List<Long> ids = processedItems.stream().map(GradingData::getSubmissionId).toList();
                        Set<Long> uniqueIds = new HashSet<>(ids);
                        assertThat(uniqueIds).hasSize(itemCount);
                    }
            );
        }
    }
}
