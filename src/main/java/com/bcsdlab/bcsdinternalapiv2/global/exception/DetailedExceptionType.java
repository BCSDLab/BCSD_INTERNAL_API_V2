package com.bcsdlab.bcsdinternalapiv2.global.exception;

import org.springframework.http.HttpStatus;

/** {@link BcsdExceptionType#withDetail}의 결과. 다시 withDetail하면 원래 타입에 새 상세를 붙인다. */
record DetailedExceptionType(BcsdExceptionType type, String detailMessage) implements BcsdExceptionType {

    @Override
    public HttpStatus getHttpStatus() {
        return type.getHttpStatus();
    }

    @Override
    public String getMessage() {
        return MESSAGE_FORMAT.formatted(type.getMessage(), detailMessage).strip();
    }

    @Override
    public BcsdExceptionType withDetail(String detailMessage) {
        return new DetailedExceptionType(type, detailMessage);
    }
}
