package com.bcsdlab.bcsdinternalapiv2.global.exception;

import org.springframework.http.HttpStatus;

public interface BcsdExceptionType {

    String MESSAGE_FORMAT = "%s \n  -> detail: %s";

    HttpStatus getHttpStatus();

    String getMessage();

    /** 디버그용 상세를 메시지 뒤에 덧붙인다. 상태 코드는 그대로다. */
    default BcsdExceptionType withDetail(String detailMessage) {
        return new DetailedExceptionType(this, detailMessage);
    }
}
