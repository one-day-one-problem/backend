package site.haruhana.www.queue.impl;

import org.springframework.stereotype.Component;
import site.haruhana.www.queue.SubmissionMessageQueue;
import site.haruhana.www.queue.wrapper.GradingData;
import site.haruhana.www.queue.wrapper.GradingRequest;

import java.util.concurrent.PriorityBlockingQueue;

@Component
public class InMemorySubmissionMessageQueue implements SubmissionMessageQueue {

    private final PriorityBlockingQueue<GradingRequest> queue = new PriorityBlockingQueue<>();

    @Override
    public void enqueue(GradingData data) {
        queue.put(GradingRequest.normal(data));
    }

    @Override
    public void prioritize(GradingData data) {
        queue.put(GradingRequest.high(data));
    }

    @Override
    public GradingData dequeue() throws InterruptedException {
        if (queue.isEmpty()) {
            return null;
        }

        return queue.take().getGradingData();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public int size() {
        return queue.size();
    }

}
