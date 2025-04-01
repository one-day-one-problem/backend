package site.haruhana.www.advice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
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

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<BaseResponse<Void>> handleExpiredJwtException(ExpiredJwtException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponse.onUnauthorized("토큰이 만료되었습니다."));
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<BaseResponse<Void>> handleUnsupportedJwtException(UnsupportedJwtException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponse.onUnauthorized("지원되지 않는 형식의 토큰입니다."));
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<BaseResponse<Void>> handleMalformedJwtException(MalformedJwtException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponse.onUnauthorized("잘못된 형식의 토큰입니다."));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<BaseResponse<Void>> handleJwtException(JwtException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponse.onUnauthorized("인증 토큰 처리 중 오류가 발생했습니다: " + e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.onBadRequest("잘못된 요청입니다: " + e.getMessage()));
    }
}
