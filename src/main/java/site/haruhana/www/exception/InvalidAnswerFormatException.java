package site.haruhana.www.exception;

public class InvalidAnswerFormatException extends RuntimeException {

    public InvalidAnswerFormatException() {
        super("올바르지 않은 답안 형식입니다. 객관식 문제의 경우 숫자만 입력 가능합니다.");
    }

}
