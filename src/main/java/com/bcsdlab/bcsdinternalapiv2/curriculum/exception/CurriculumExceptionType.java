package com.bcsdlab.bcsdinternalapiv2.curriculum.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import org.springframework.http.HttpStatus;

public enum CurriculumExceptionType implements BcsdExceptionType {

    CURRICULUM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 커리큘럼입니다."),
    ;

    private final HttpStatus status;
    private final String message;

    CurriculumExceptionType(HttpStatus status, String message) {
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
        return new DetailedCurriculumExceptionType(this, detailMessage);
    }

    private record DetailedCurriculumExceptionType(CurriculumExceptionType type, String detailMessage)
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
            return new DetailedCurriculumExceptionType(type, detailMessage);
        }
    }
}
