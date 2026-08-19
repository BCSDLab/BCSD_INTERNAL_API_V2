package com.bcsdlab.bcsdinternalapiv2.activity.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import org.springframework.http.HttpStatus;

public enum ActivityExceptionType implements BcsdExceptionType {

    ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 활동입니다."),
    ;

    private final HttpStatus status;
    private final String message;

    ActivityExceptionType(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public BcsdExceptionType withDetail(String detailMessage) {
        return new DetailedActivityExceptionType(this, detailMessage);
    }

    private record DetailedActivityExceptionType(ActivityExceptionType type, String detailMessage)
            implements BcsdExceptionType {

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
            return new DetailedActivityExceptionType(type, detailMessage);
        }
    }
}
