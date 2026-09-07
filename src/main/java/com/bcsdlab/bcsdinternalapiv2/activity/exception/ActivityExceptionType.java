package com.bcsdlab.bcsdinternalapiv2.activity.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import org.springframework.http.HttpStatus;

public enum ActivityExceptionType implements BcsdExceptionType {

    ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 활동입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 활동 카테고리입니다."),
    CATEGORY_SLUG_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 카테고리 주소입니다."),
    CATEGORY_HAS_ACTIVITIES(HttpStatus.CONFLICT, "활동이 남아 있는 카테고리는 삭제할 수 없습니다."),
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
