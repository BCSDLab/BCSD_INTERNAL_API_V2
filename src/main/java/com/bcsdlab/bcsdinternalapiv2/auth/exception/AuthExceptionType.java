package com.bcsdlab.bcsdinternalapiv2.auth.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import org.springframework.http.HttpStatus;

public enum AuthExceptionType implements BcsdExceptionType {

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "학번 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_LOCKED(HttpStatus.LOCKED, "로그인 시도 초과로 계정이 잠겼습니다. 잠시 후 다시 시도해 주세요."),
    ACCOUNT_WITHDRAWN(HttpStatus.UNAUTHORIZED, "사용할 수 없는 계정입니다. 관리자에게 문의해 주세요."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    SETUP_REQUIRED(HttpStatus.FORBIDDEN, "최초 로그인 정보 입력이 필요합니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "다시 로그인해 주세요."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다. 다시 로그인해 주세요."),
    REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "보안상의 이유로 로그아웃되었습니다. 다시 로그인해 주세요."),
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    RESET_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 링크입니다."),
    RESET_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 링크입니다. 다시 요청해 주세요."),
    RESET_TOKEN_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용된 링크입니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."),
    ;

    private final HttpStatus status;
    private final String message;

    AuthExceptionType(HttpStatus status, String message) {
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
        return new DetailedAuthExceptionType(this, detailMessage);
    }

    private record DetailedAuthExceptionType(AuthExceptionType type, String detailMessage)
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
            return new DetailedAuthExceptionType(type, detailMessage);
        }
    }
}
