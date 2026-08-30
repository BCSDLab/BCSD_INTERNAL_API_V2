package com.bcsdlab.bcsdinternalapiv2.home.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import org.springframework.http.HttpStatus;

public enum HomeExceptionType implements BcsdExceptionType {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 부원입니다."),
    MENTOR_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "이미 노출 중인 멘토입니다."),
    MENTOR_SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "노출 중이지 않은 멘토입니다."),
    QNA_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 질문입니다."),
    INVALID_GOOGLE_FORM_URL(HttpStatus.BAD_REQUEST, "forms.gle 또는 docs.google.com/forms 주소만 등록할 수 있습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    HomeExceptionType(HttpStatus status, String message) {
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
        return new DetailedHomeExceptionType(this, detailMessage);
    }

    private record DetailedHomeExceptionType(HomeExceptionType type, String detailMessage)
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
            return new DetailedHomeExceptionType(type, detailMessage);
        }
    }
}
