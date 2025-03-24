package site.haruhana.www.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.haruhana.www.dto.BaseResponse;
import site.haruhana.www.exception.InvalidAnswerFormatException;
import site.haruhana.www.exception.ProblemNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProblemNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleProblemNotFoundException(ProblemNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(BaseResponse.onNotFound(e.getMessage()));
    }

    @ExceptionHandler(InvalidAnswerFormatException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidAnswerFormatException(InvalidAnswerFormatException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.onBadRequest(e.getMessage()));
    }

}
