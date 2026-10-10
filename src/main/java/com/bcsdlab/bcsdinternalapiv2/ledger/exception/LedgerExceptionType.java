package com.bcsdlab.bcsdinternalapiv2.ledger.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import org.springframework.http.HttpStatus;

public enum LedgerExceptionType implements BcsdExceptionType {

    INVALID_SEMESTER_ID(HttpStatus.BAD_REQUEST, "학기 형식이 올바르지 않습니다."),
    SEMESTER_NOT_FOUND(HttpStatus.NOT_FOUND, "학기 회비를 찾을 수 없습니다."),
    SEMESTER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 생성된 학기 회비입니다."),
    SEMESTER_NOT_CREATABLE(HttpStatus.BAD_REQUEST, "현재 학기의 바로 다음 학기까지만 만들 수 있습니다."),
    NOT_ROSTER_MEMBER(HttpStatus.NOT_FOUND, "이 학기 회비 명단에 없는 회원입니다."),
    ROSTER_MEMBER_EXISTS(HttpStatus.CONFLICT, "이미 이 학기 명단에 있는 회원입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    LedgerExceptionType(HttpStatus status, String message) {
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
        return new DetailedLedgerExceptionType(this, detailMessage);
    }

    private record DetailedLedgerExceptionType(LedgerExceptionType type, String detailMessage)
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
            return new DetailedLedgerExceptionType(type, detailMessage);
        }
    }
}
