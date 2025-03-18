package site.haruhana.www.queue.impl;

import org.springframework.stereotype.Component;
import site.haruhana.www.entity.submission.Submission;
import site.haruhana.www.queue.SubmissionMessageQueue;
import site.haruhana.www.queue.wrapper.GradingRequest;

import java.util.concurrent.PriorityBlockingQueue;

@Component
public class InMemorySubmissionMessageQueue implements SubmissionMessageQueue {

    private final PriorityBlockingQueue<GradingRequest> queue = new PriorityBlockingQueue<>();

    @Override
    public void enqueue(Submission submission) {
        queue.put(GradingRequest.normal(submission));
    }

    @Override
    public Submission dequeue() throws InterruptedException {
        if (queue.isEmpty()) {
            return null;
        }

        return queue.take().getSubmission();
    }

    @Override
    public void prioritize(Submission submission) {
        queue.put(GradingRequest.high(submission));
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
