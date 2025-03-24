package site.haruhana.www.exception;

public class SubmissionNotFoundException extends RuntimeException {
    public SubmissionNotFoundException() {
        super("제출 정보가 존재하지 않습니다.");
    }
}
