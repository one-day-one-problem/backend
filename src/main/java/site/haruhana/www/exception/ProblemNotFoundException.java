package site.haruhana.www.exception;

public class ProblemNotFoundException extends RuntimeException {
    public ProblemNotFoundException() {
        super("존재하지 않는 문제입니다.");
    }
}
