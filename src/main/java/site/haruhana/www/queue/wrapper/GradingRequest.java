package site.haruhana.www.queue.wrapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import site.haruhana.www.entity.submission.Submission;

/**
 * 채점 요청을 관리하기 위한 래퍼 클래스
 * <p>
 * Submission 객체에 우선순위(priority)와 요청 시간(enqueuedAt)을 추가하여
 * 우선순위 그룹 내에서 FIFO 순서를 보장한다.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GradingRequest implements Comparable<GradingRequest> {

    // 채점이 필요한 주관식 답안
    private final Submission submission;

    // 채점 요청의 우선순위
    private final int priority;

    // 요청이 큐에 추가된 시간 (나노초 단위로 정확도 보장)
    private final long enqueuedAt;

    // 우선순위 상수
    private final static int HIGHEST_PRIORITY = 0;
    private final static int NORMAL_PRIORITY = 10;

    @Override
    public int compareTo(GradingRequest other) {
        if (this.priority == other.priority) {
            return Long.compare(this.enqueuedAt, other.enqueuedAt);
        }

        return Integer.compare(this.priority, other.priority);
    }

    /**
     * 일반 우선순위(10)의 채점 요청을 생성하는 팩토리 메서드
     *
     * @param submission 채점이 필요한 주관식 답안
     * @return 일반 우선순위의 GradingRequest 객체
     */
    public static GradingRequest normal(Submission submission) {
        return new GradingRequest(submission, NORMAL_PRIORITY, System.nanoTime());
    }

    /**
     * 높은 우선순위(0)의 채점 요청을 생성하는 팩토리 메서드
     *
     * @param submission 채점이 필요한 주관식 답안
     * @return 높은 우선순위의 GradingRequest 객체
     */
    public static GradingRequest high(Submission submission) {
        return new GradingRequest(submission, HIGHEST_PRIORITY, System.nanoTime());
    }

}