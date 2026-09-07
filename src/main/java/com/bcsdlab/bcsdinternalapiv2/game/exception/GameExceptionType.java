package com.bcsdlab.bcsdinternalapiv2.game.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import org.springframework.http.HttpStatus;

public enum GameExceptionType implements BcsdExceptionType {

    GAME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게임입니다."),
    GAME_SLUG_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 주소입니다."),
    GAME_BUILD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 빌드입니다."),
    INVALID_CONTENT_DESCRIPTOR(HttpStatus.BAD_REQUEST, "내용정보 항목이 올바르지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 부원입니다."),
    MEMBER_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "이미 이 게임에 배정된 부원입니다."),
    GAME_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "이 게임에 배정되지 않은 부원입니다."),
    GAME_BUILD_INVALID_STATE(HttpStatus.CONFLICT, "이 상태의 빌드에는 처리할 수 없습니다."),
    GAME_BUILD_WEBHOOK_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "웹훅 시크릿이 올바르지 않습니다."),
    GAME_BUILD_INVALID_WEBHOOK_STATUS(HttpStatus.BAD_REQUEST, "status는 ACTIVE 또는 FAILED만 가능합니다."),
    ;

    private final HttpStatus status;
    private final String message;

    GameExceptionType(HttpStatus status, String message) {
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
        return new DetailedGameExceptionType(this, detailMessage);
    }

    private record DetailedGameExceptionType(GameExceptionType type, String detailMessage)
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
            return new DetailedGameExceptionType(type, detailMessage);
        }
    }
}
