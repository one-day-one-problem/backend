package site.haruhana.www.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.haruhana.www.dto.submission.request.SubmissionRequestDto;
import site.haruhana.www.dto.submission.response.extend.MultipleChoiceSubmissionResponseDto;
import site.haruhana.www.dto.submission.response.extend.SubjectiveSubmissionResponseDto;
import site.haruhana.www.dto.submission.response.SubmissionResponseDto;
import site.haruhana.www.entity.problem.Problem;
import site.haruhana.www.entity.problem.ProblemCategory;
import site.haruhana.www.entity.problem.ProblemDifficulty;
import site.haruhana.www.entity.problem.ProblemProvider;
import site.haruhana.www.entity.problem.choice.ProblemOption;
import site.haruhana.www.entity.submission.Submission;
import site.haruhana.www.entity.user.Role;
import site.haruhana.www.entity.user.User;
import site.haruhana.www.exception.ProblemNotFoundException;
import site.haruhana.www.queue.SubmissionMessageQueue;
import site.haruhana.www.queue.message.GradingData;
import site.haruhana.www.repository.ProblemRepository;
import site.haruhana.www.repository.SubmissionRepository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceUnitTest {

    @Mock
    private SubmissionMessageQueue messageQueue;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private SubmissionService submissionService;

    private User testUser;
    private Problem multipleChoiceProblem;
    private Problem subjectiveProblem;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .id(1L)
                .name("테스트 사용자")
                .email("test@example.com")
                .role(Role.USER)
                .build();

        // 테스트용 문제 생성
        multipleChoiceProblem = createMultipleChoiceProblem();
        subjectiveProblem = createSubjectiveProblem();
    }

    @Nested
    @DisplayName("객관식 문제 채점")
    class MultipleChoiceSubmissionTest {

        @Test
        @DisplayName("모든 정답을 선택했을 경우 정답으로 처리된다")
        void shouldMarkCorrectWhenAllCorrectOptionsSelected() {
            // given: 정답이 여러 개인 객관식 문제와 모든 정답을 선택한 제출 요청이 주어졌을 때
            when(problemRepository.findById(1L)).thenReturn(Optional.of(multipleChoiceProblem));

            Submission savedSubmission = Submission.builder()
                    .id(1L)
                    .user(testUser)
                    .problem(multipleChoiceProblem)
                    .submittedAt(LocalDateTime.now())
                    .duration(120)
                    .submittedAnswer("1,3")
                    .isCorrect(true)
                    .build();

            when(submissionRepository.save(any(Submission.class))).thenReturn(savedSubmission);

            SubmissionRequestDto requestDto = new SubmissionRequestDto("1,3", 120);

            // when: 답안을 제출하면
            SubmissionResponseDto responseDto = submissionService.submitAnswer(testUser, 1L, requestDto);

            // then: 제출이 정답으로 채점된다
            var multipleChoiceResponse = assertInstanceOf(MultipleChoiceSubmissionResponseDto.class, responseDto);
            assertAll(
                    "정답 제출 검증",
                    () -> assertThat(multipleChoiceResponse.getIsCorrect()).isTrue(),
                    () -> verify(submissionRepository, times(1)).save(any(Submission.class)),
                    () -> verify(messageQueue, never()).enqueue(any(GradingData.class))
            );
        }

        @Test
        @DisplayName("일부 정답만 선택했을 경우 오답으로 처리된다")
        void shouldMarkIncorrectWhenOnlyPartialCorrectOptionsSelected() {
            // given: 정답이 여러 개인 객관식 문제와 일부 정답만 선택한 제출 요청이 주어졌을 때
            when(problemRepository.findById(1L)).thenReturn(Optional.of(multipleChoiceProblem));

            Submission savedSubmission = Submission.builder()
                    .id(1L)
                    .user(testUser)
                    .problem(multipleChoiceProblem)
                    .submittedAt(LocalDateTime.now())
                    .duration(120)
                    .submittedAnswer("1")
                    .isCorrect(false)
                    .build();

            when(submissionRepository.save(any(Submission.class))).thenReturn(savedSubmission);

            SubmissionRequestDto requestDto = new SubmissionRequestDto("1", 120);

            // when: 답안을 제출하면
            SubmissionResponseDto responseDto = submissionService.submitAnswer(testUser, 1L, requestDto);

            // then: 제출이 오답으로 채점된다
            var multipleChoiceResponse = assertInstanceOf(MultipleChoiceSubmissionResponseDto.class, responseDto);
            assertAll(
                    "부분 정답 제출 검증",
                    () -> assertThat(multipleChoiceResponse.getIsCorrect()).isFalse(),
                    () -> verify(submissionRepository, times(1)).save(any(Submission.class)),
                    () -> verify(messageQueue, never()).enqueue(any(GradingData.class))
            );
        }

        @Test
        @DisplayName("정답 외에 오답도 선택했을 경우 오답으로 처리된다")
        void shouldMarkIncorrectWhenCorrectAndIncorrectOptionsSelected() {
            // given: 정답이 여러 개인 객관식 문제와 정답과 오답을 함께 선택한 제출 요청이 주어졌을 때
            when(problemRepository.findById(1L)).thenReturn(Optional.of(multipleChoiceProblem));

            Submission savedSubmission = Submission.builder()
                    .id(1L)
                    .user(testUser)
                    .problem(multipleChoiceProblem)
                    .submittedAt(LocalDateTime.now())
                    .duration(120)
                    .submittedAnswer("1,2,3")
                    .isCorrect(false)
                    .build();

            when(submissionRepository.save(any(Submission.class))).thenReturn(savedSubmission);

            SubmissionRequestDto requestDto = new SubmissionRequestDto("1,2,3", 120);

            // when: 답안을 제출하면
            SubmissionResponseDto responseDto = submissionService.submitAnswer(testUser, 1L, requestDto);

            // then: 제출이 오답으로 채점된다
            var multipleChoiceResponse = assertInstanceOf(MultipleChoiceSubmissionResponseDto.class, responseDto);
            assertAll(
                    "정답+오답 혼합 제출 검증",
                    () -> assertThat(multipleChoiceResponse.getIsCorrect()).isFalse(),
                    () -> verify(submissionRepository, times(1)).save(any(Submission.class)),
                    () -> verify(messageQueue, never()).enqueue(any(GradingData.class))
            );
        }

        @Test
        @DisplayName("완전히 다른 답을 선택했을 경우 오답으로 처리된다")
        void shouldMarkIncorrectWhenAllIncorrectOptionsSelected() {
            // given: 정답이 여러 개인 객관식 문제와 모두 오답을 선택한 제출 요청이 주어졌을 때
            when(problemRepository.findById(1L)).thenReturn(Optional.of(multipleChoiceProblem));

            Submission savedSubmission = Submission.builder()
                    .id(1L)
                    .user(testUser)
                    .problem(multipleChoiceProblem)
                    .submittedAt(LocalDateTime.now())
                    .duration(120)
                    .submittedAnswer("2,4")
                    .isCorrect(false)
                    .build();

            when(submissionRepository.save(any(Submission.class))).thenReturn(savedSubmission);

            SubmissionRequestDto requestDto = new SubmissionRequestDto("2,4", 120);

            // when: 답안을 제출하면
            SubmissionResponseDto responseDto = submissionService.submitAnswer(testUser, 1L, requestDto);

            // then: 제출이 오답으로 채점된다
            var multipleChoiceResponse = assertInstanceOf(MultipleChoiceSubmissionResponseDto.class, responseDto);
            assertAll(
                    "오답 제출 검증",
                    () -> assertThat(multipleChoiceResponse.getIsCorrect()).isFalse(),
                    () -> verify(submissionRepository, times(1)).save(any(Submission.class)),
                    () -> verify(messageQueue, never()).enqueue(any(GradingData.class))
            );
        }

        @Test
        @DisplayName("빈 답안을 제출하면 오답으로 처리된다")
        void shouldMarkIncorrectWhenEmptyAnswerSubmitted() {
            // given: 정답이 여러 개인 객관식 문제와 빈 답안이 주어졌을 때
            when(problemRepository.findById(1L)).thenReturn(Optional.of(multipleChoiceProblem));

            Submission savedSubmission = Submission.builder()
                    .id(1L)
                    .user(testUser)
                    .problem(multipleChoiceProblem)
                    .submittedAt(LocalDateTime.now())
                    .duration(120)
                    .submittedAnswer("")
                    .isCorrect(false)
                    .build();

            when(submissionRepository.save(any(Submission.class))).thenReturn(savedSubmission);

            SubmissionRequestDto requestDto = new SubmissionRequestDto("", 120);

            // when: 답안을 제출하면
            SubmissionResponseDto responseDto = submissionService.submitAnswer(testUser, 1L, requestDto);

            // then: 제출이 오답으로 채점된다
            var multipleChoiceResponse = assertInstanceOf(MultipleChoiceSubmissionResponseDto.class, responseDto);
            assertAll(
                    "빈 답안 제출 검증",
                    () -> assertThat(multipleChoiceResponse.getIsCorrect()).isFalse(),
                    () -> assertThat(responseDto.getId()).isEqualTo(1L),
                    () -> verify(submissionRepository, times(1)).save(any(Submission.class)),
                    () -> verify(messageQueue, never()).enqueue(any(GradingData.class))
            );
        }
    }

    @Nested
    @DisplayName("주관식 문제 제출")
    class SubjectiveSubmissionTest {

        @Test
        @DisplayName("주관식 문제 제출은 채점 큐에 추가된다")
        void shouldAddToQueueWhenSubjectiveSubmitted() {
            // given: 주관식 문제와 답안이 주어졌을 때
            when(problemRepository.findById(2L)).thenReturn(Optional.of(subjectiveProblem));

            Submission savedSubmission = Submission.builder()
                    .id(1L)
                    .user(testUser)
                    .problem(subjectiveProblem)
                    .submittedAt(LocalDateTime.now())
                    .duration(300)
                    .submittedAnswer("주관식 답안 내용입니다.")
                    .build();

            when(submissionRepository.save(any(Submission.class))).thenReturn(savedSubmission);

            SubmissionRequestDto requestDto = new SubmissionRequestDto("주관식 답안 내용입니다.", 300);

            // when: 답안을 제출하면
            SubmissionResponseDto responseDto = submissionService.submitAnswer(testUser, 2L, requestDto);

            // then: 채점 큐에 추가되고 pending 상태로 응답된다
            ArgumentCaptor<GradingData> gradingDataCaptor = ArgumentCaptor.forClass(GradingData.class);
            var subjectiveResponse = assertInstanceOf(SubjectiveSubmissionResponseDto.class, responseDto);

            assertAll(
                    "주관식 제출 처리 검증",
                    () -> assertThat(subjectiveResponse.getIsPending()).isTrue(),
                    () -> assertThat(subjectiveResponse.getFeedback()).isNull(),
                    () -> assertThat(subjectiveResponse.getScore()).isNull(),
                    () -> verify(messageQueue, times(1)).enqueue(gradingDataCaptor.capture()),
                    () -> {
                        GradingData capturedData = gradingDataCaptor.getValue();
                        assertThat(capturedData.getSubmissionId()).isEqualTo(1L);
                        assertThat(capturedData.getProblemId()).isEqualTo(2L);
                        assertThat(capturedData.getSubmittedAnswer()).isEqualTo("주관식 답안 내용입니다.");
                    },
                    () -> verify(submissionRepository, times(1)).save(any(Submission.class))
            );
        }
    }

    @Test
    @DisplayName("존재하지 않는 문제 ID로 제출하면 예외가 발생한다")
    void shouldThrowExceptionWhenProblemNotExists() {
        // given: 존재하지 않는 문제 ID가 주어졌을 때
        when(problemRepository.findById(999L)).thenReturn(Optional.empty());

        SubmissionRequestDto requestDto = new SubmissionRequestDto("1,2", 60);

        // when & then: 제출 시 예외가 발생한다
        assertAll(
                "존재하지 않는 문제 제출 검증",
                () -> assertThrows(ProblemNotFoundException.class, () -> submissionService.submitAnswer(testUser, 999L, requestDto)),
                () -> verify(submissionRepository, never()).save(any(Submission.class))
        );
    }

    @Test
    @DisplayName("제출 후 문제 풀이 수가 증가한다")
    void shouldIncrementSolvedCountAfterSubmission() {
        // given: 객관식 문제와 답안이 주어졌을 때
        when(problemRepository.findById(1L)).thenReturn(Optional.of(multipleChoiceProblem));

        Submission savedSubmission = Submission.builder()
                .id(1L)
                .user(testUser)
                .problem(multipleChoiceProblem)
                .submittedAt(LocalDateTime.now())
                .duration(120)
                .submittedAnswer("1,3")
                .isCorrect(true)
                .build();

        when(submissionRepository.save(any(Submission.class))).thenReturn(savedSubmission);

        SubmissionRequestDto requestDto = new SubmissionRequestDto("1,3", 120);

        // when: 답안을 제출하면
        submissionService.submitAnswer(testUser, 1L, requestDto);

        // then: 문제 풀이 수가 증가한다
        assertThat(multipleChoiceProblem.getSolvedCount()).isEqualTo(1);
    }

    /**
     * 테스트용 객관식 문제를 생성하는 helper 메서드
     */
    private Problem createMultipleChoiceProblem() {
        Problem problem = Problem.multipleChoiceProblemBuilder()
                .title("Spring IoC 컨테이너의 역할은?")
                .question("스프링 프레임워크에서 IoC(Inversion of Control) 컨테이너의 주요 역할로 올바른 것을 모두 고르시오.")
                .category(ProblemCategory.SPRING_IOC)
                .difficulty(ProblemDifficulty.MEDIUM)
                .provider(ProblemProvider.AI)
                .build();

        // ID 설정
        setFieldValue(problem, "id", 1L);

        // 4개의 선택지 추가 (1번, 3번이 정답)
        addProblemOption(problem, 1L, "빈(Bean)의 생명주기 관리", true);
        addProblemOption(problem, 2L, "데이터베이스 연결 풀 직접 관리", false);
        addProblemOption(problem, 3L, "객체 간의 의존성 주입", true);
        addProblemOption(problem, 4L, "HTML 렌더링 최적화", false);

        return problem;
    }

    /**
     * 테스트용 주관식 문제를 생성하는 helper 메서드
     */
    private Problem createSubjectiveProblem() {
        Problem problem = Problem.subjectiveProblemBuilder()
                .title("스프링 AOP 개념 설명")
                .question("스프링 프레임워크의 AOP(Aspect-Oriented Programming)가 무엇인지 설명하고, 활용 예시를 들어보세요.")
                .category(ProblemCategory.SPRING_AOP)
                .difficulty(ProblemDifficulty.HARD)
                .provider(ProblemProvider.AI)
                .expectedAnswerLength("300-500자")
                .sampleAnswer("AOP는 관점 지향 프로그래밍으로, 핵심 비즈니스 로직과 공통 관심사(로깅, 트랜잭션 등)를 분리하여 모듈화하는 프로그래밍 패러다임입니다. 스프링에서는 프록시 기반으로 구현되며, @Aspect, @Before, @After 등의 어노테이션을 활용합니다...")
                .build();

        // ID 설정
        setFieldValue(problem, "id", 2L);

        // 채점 기준 추가
        problem.addGradingCriteria("AOP의 개념을 정확하게 설명했는가?");
        problem.addGradingCriteria("Cross-cutting concerns의 예시를 들었는가?");
        problem.addGradingCriteria("스프링 AOP의 구현 방식을 설명했는가?");
        problem.addGradingCriteria("실제 활용 예시가 적절한가?");

        return problem;
    }

    /**
     * 객관식 문제에 선택지를 추가하는 helper 메서드
     */
    private void addProblemOption(Problem problem, Long optionId, String content, boolean isCorrect) {
        ProblemOption option = ProblemOption.builder()
                .problem(problem)
                .content(content)
                .isCorrect(isCorrect)
                .build();

        // ID 설정
        setFieldValue(option, "id", optionId);

        // 문제에 옵션 추가
        List<ProblemOption> options = problem.getProblemOptions();
        options.add(option);
    }

    /**
     * 리플렉션을 사용하여 private 필드 값을 설정하는 helper 메서드
     */
    private void setFieldValue(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);

        } catch (Exception e) {
            throw new RuntimeException("필드 값 설정 중 오류 발생", e);
        }
    }

}
