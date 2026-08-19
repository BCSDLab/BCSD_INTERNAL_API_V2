package com.bcsdlab.bcsdinternalapiv2.track.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import org.springframework.http.HttpStatus;

public enum TrackExceptionType implements BcsdExceptionType {

    TRACK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 트랙입니다."),
    TRACK_CODE_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 트랙 코드입니다."),
    TRACK_PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 트랙 페이지입니다."),
    TRACK_PAGE_SLUG_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 주소입니다."),
    TRACK_PAGE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 홈페이지 프로필이 있는 트랙입니다."),
    ;

    private final HttpStatus status;
    private final String message;

    TrackExceptionType(HttpStatus status, String message) {
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
        return new DetailedTrackExceptionType(this, detailMessage);
    }

    private record DetailedTrackExceptionType(TrackExceptionType type, String detailMessage)
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
            return new DetailedTrackExceptionType(type, detailMessage);
        }
    }
}
