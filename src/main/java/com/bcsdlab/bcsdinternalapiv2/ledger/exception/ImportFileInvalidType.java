package com.bcsdlab.bcsdinternalapiv2.ledger.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import org.springframework.http.HttpStatus;

/**
 * 가져오기 파일 오류. 화면에 그대로 띄우는 문장이라 withDetail(디버그용 "-> detail:" 형식) 대신 이유를 괄호로 붙인다.
 * 이유에는 행 번호·열 이름만 넣고 셀 값(이름·금액)은 넣지 않는다.
 */
public record ImportFileInvalidType(String reason) implements BcsdExceptionType {

    public static LedgerException of(String reason) {
        return new LedgerException(new ImportFileInvalidType(reason));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return LedgerExceptionType.IMPORT_FILE_INVALID.getHttpStatus();
    }

    @Override
    public String getMessage() {
        return "%s (%s)".formatted(LedgerExceptionType.IMPORT_FILE_INVALID.getMessage(), reason);
    }

    @Override
    public BcsdExceptionType withDetail(String detailMessage) {
        return LedgerExceptionType.IMPORT_FILE_INVALID.withDetail(detailMessage);
    }
}
