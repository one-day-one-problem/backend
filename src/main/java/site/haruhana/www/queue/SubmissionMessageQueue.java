package site.haruhana.www.queue;

import site.haruhana.www.queue.wrapper.GradingData;

/**
 * 주관식 답안 채점을 위한 메시지 큐 인터페이스
 * <p>
 * 채점할 답안을 관리하며, 우선순위에 따라 처리 순서를 조정할 수 있다.
 */
public interface SubmissionMessageQueue {

    /**
     * 주관식 답안 채점 요청을 일반 우선순위로 채점 대기 큐에 추가하는 메서드
     *
     * @param data 채점할 데이터
     */
    void enqueue(GradingData data);

    /**
     * 주관식 답안 채점 요청을 높은 우선순위로 채점 대기 큐에 추가하는 메서드
     * <p>
     * 일반적인 FIFO 순서 대신, 해당 답안을 우선적으로 채점해야 할 때 사용된다.
     * 예: 채점 실패 후 재시도, 긴급 요청 등.
     *
     * @param data 채점할 데이터
     */
    void prioritize(GradingData data);

    /**
     * 채점 대기 큐에서 가장 우선순위가 높은 채점 요청을 꺼내는 메서드
     *
     * @return 채점할 데이터
     * @throws InterruptedException 큐가 비어 있을 때 스레드가 인터럽트되면 발생
     */
    GradingData dequeue() throws InterruptedException;

    /**
     * 채점 대기 큐가 비어 있는지 확인하는 메서드
     *
     * @return 채점 대기 큐가 비어 있으면 true, 그렇지 않으면 false
     */
    boolean isEmpty();

    /**
     * 현재 채점을 대기 중인 답안의 개수를 반환하는 메서드
     *
     * @return 채점 대기 큐에서 채점 대기 중인 답안의 수
     */
    int size();

}