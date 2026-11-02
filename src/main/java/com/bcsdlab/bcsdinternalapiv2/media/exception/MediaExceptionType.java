package com.bcsdlab.bcsdinternalapiv2.media.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import org.springframework.http.HttpStatus;

public enum MediaExceptionType implements BcsdExceptionType {

    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 이미지입니다."),
    PURPOSE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "이 API에서 쓸 수 없는 이미지 유형입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 부원입니다."),
    IMAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "이 유형의 이미지를 다룰 권한이 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    MediaExceptionType(HttpStatus status, String message) {
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
}
