package com.bcsdlab.bcsdinternalapiv2.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, Exception e) {
        log.error("[{}] 예상하지 못한 예외가 발생했습니다. uri: {} {}", request.getMethod(), request.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("알 수 없는 오류가 발생했습니다."));
    }

    private void logRequest(HttpServletRequest request, Exception e) {
        log.warn("잘못된 요청입니다. uri: {} {} exception={} message={}",
                request.getMethod(), request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage());
    }
}
