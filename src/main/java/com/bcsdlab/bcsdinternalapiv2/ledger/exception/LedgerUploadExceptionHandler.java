package com.bcsdlab.bcsdinternalapiv2.ledger.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.ErrorResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.AdminLedgerImportController;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * 거래내역 업로드 오류. 지금 레포에서 multipart를 받는 API는 가져오기 미리보기뿐이다.
 *
 * <p>업로드 크기 초과는 컨트롤러를 고르기 전에(multipart 해석 단계) 나므로 범위를 좁힌 advice로는 잡히지 않는다.
 * 그래서 이 클래스는 전역으로 두되, 크기 초과만 다루고 경로로 메시지를 고른다. 이 처리기가 없으면 전역 처리기가
 * 500으로 응답한다.
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LedgerUploadExceptionHandler {

    private static final String IMPORT_PATH = "/v1/admin/ledger/imports";

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(HttpServletRequest request,
                                                             MaxUploadSizeExceededException e) {
        log.warn("업로드 크기 초과. uri: {} {}", request.getMethod(), request.getRequestURI());
        String message = request.getRequestURI().startsWith(IMPORT_PATH)
                ? LedgerExceptionType.IMPORT_FILE_TOO_LARGE.getMessage()
                : "업로드 파일이 너무 큽니다.";
        return ResponseEntity.status(LedgerExceptionType.IMPORT_FILE_TOO_LARGE.getHttpStatus())
                .body(new ErrorResponse(message));
    }

    /** 가져오기 컨트롤러에서만: file 파트가 없거나 multipart 요청이 아니면 400이다(전역 처리기는 500). */
    @RestControllerAdvice(assignableTypes = AdminLedgerImportController.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static class ImportRequestExceptionHandler {

        @ExceptionHandler({MissingServletRequestPartException.class, MultipartException.class})
        public ResponseEntity<ErrorResponse> handleMissingFile(HttpServletRequest request, Exception e) {
            log.warn("거래내역 파일 없음. uri: {} {} exception={}", request.getMethod(), request.getRequestURI(),
                    e.getClass().getSimpleName());
            ImportFileInvalidType type = new ImportFileInvalidType("업로드할 파일(file)이 없습니다");
            return ResponseEntity.status(type.getHttpStatus()).body(new ErrorResponse(type.getMessage()));
        }
    }
}
