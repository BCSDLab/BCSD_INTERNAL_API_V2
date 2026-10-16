package com.bcsdlab.bcsdinternalapiv2.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BcsdException.class)
    public ResponseEntity<ErrorResponse> handleBcsdException(HttpServletRequest request, BcsdException e) {
        BcsdExceptionType exceptionType = e.getExceptionType();
        logRequest(request, e);
        return ResponseEntity.status(exceptionType.getHttpStatus()).body(new ErrorResponse(exceptionType.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            HttpServletRequest request,
            MethodArgumentNotValidException e
    ) {
        logRequest(request, e);
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("잘못된 요청입니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpServletRequest request,
            HttpMessageNotReadableException e
    ) {
        logRequest(request, e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("요청 본문을 읽을 수 없습니다."));
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleJpaException(HttpServletRequest request, Exception e) {
        logRequest(request, e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("제약조건에 위배되는 값입니다."));
    }

    /** 없는 경로(제거된 API 포함)는 500이 아니라 404다. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(HttpServletRequest request,
                                                                        NoResourceFoundException e) {
        logRequest(request, e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("요청한 API를 찾을 수 없습니다."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            HttpServletRequest request,
            MissingServletRequestParameterException e
    ) {
        logRequest(request, e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("필수 요청 파라미터 '%s'가 없습니다.".formatted(e.getParameterName())));
    }

    /** 경로·쿼리 값의 형식 오류(숫자가 아닌 id, 없는 enum 값, 잘못된 날짜 등). */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(HttpServletRequest request,
                                                                     TypeMismatchException e) {
        logRequest(request, e);
        String name = e.getPropertyName() != null ? e.getPropertyName() : "요청 값";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("'%s'의 형식이 올바르지 않습니다.".formatted(name)));
    }

    /** 정렬 파라미터에 없는 필드를 지정한 경우. */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReferenceException(HttpServletRequest request,
                                                                          PropertyReferenceException e) {
        logRequest(request, e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("정렬할 수 없는 필드입니다: " + e.getPropertyName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, Exception e) {
        // 405·415·406, 헤더 누락 등 Spring MVC 예외는 스스로 4xx 상태를 갖고 있다. 이를 500으로 바꾸지 않는다.
        if (e instanceof org.springframework.web.ErrorResponse springError
                && springError.getStatusCode().is4xxClientError()) {
            logRequest(request, e);
            HttpStatusCode status = springError.getStatusCode();
            return ResponseEntity.status(status).body(new ErrorResponse(clientErrorMessage(status)));
        }
        log.error("[{}] 예상하지 못한 예외가 발생했습니다. uri: {} {}", request.getMethod(), request.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("알 수 없는 오류가 발생했습니다."));
    }

    private static String clientErrorMessage(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> "요청한 API를 찾을 수 없습니다.";
            case 405 -> "지원하지 않는 HTTP 메서드입니다.";
            case 406 -> "지원하지 않는 응답 형식입니다.";
            case 415 -> "지원하지 않는 Content-Type입니다.";
            default -> "잘못된 요청입니다.";
        };
    }

    private void logRequest(HttpServletRequest request, Exception e) {
        log.warn("잘못된 요청입니다. uri: {} {} exception={} message={}",
                request.getMethod(), request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage());
    }
}
