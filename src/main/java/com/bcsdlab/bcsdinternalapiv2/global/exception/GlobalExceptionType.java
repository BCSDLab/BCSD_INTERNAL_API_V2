package com.bcsdlab.bcsdinternalapiv2.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 특정 도메인에 속하지 않고 여러 도메인이 공유하는 예외 타입.
 * 예: 정렬 순서 변경 요청 검증({@link com.bcsdlab.bcsdinternalapiv2.global.util.DisplayOrders})은
 * 트랙·커리큘럼·활동 등 모든 도메인에서 동일한 규칙과 동일한 HTTP 상태를 쓴다.
 */
public enum GlobalExceptionType implements BcsdExceptionType {

    ORDER_IDS_MISMATCH(HttpStatus.BAD_REQUEST, "순서 변경 대상이 기존 항목과 일치하지 않습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    GlobalExceptionType(HttpStatus status, String message) {
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
        return new DetailedGlobalExceptionType(this, detailMessage);
    }

    private record DetailedGlobalExceptionType(GlobalExceptionType type, String detailMessage)
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
            return new DetailedGlobalExceptionType(type, detailMessage);
        }
    }
}
