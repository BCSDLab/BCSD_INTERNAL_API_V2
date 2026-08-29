package com.bcsdlab.bcsdinternalapiv2.reservation.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import org.springframework.http.HttpStatus;

public enum ReservationExceptionType implements BcsdExceptionType {

    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."),
    RESERVATION_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 반복 예약 그룹입니다."),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "예약 시간 범위가 올바르지 않습니다."),
    SLOT_NOT_ALIGNED(HttpStatus.BAD_REQUEST, "예약 시간은 30분 단위여야 합니다."),
    PAST_TIME(HttpStatus.BAD_REQUEST, "이미 지난 시간은 예약할 수 없습니다."),
    OUT_OF_SINGLE_BOOKING_WINDOW(HttpStatus.BAD_REQUEST, "예약은 2주 이내의 날짜만 가능합니다."),
    OUT_OF_REPEAT_WINDOW(HttpStatus.BAD_REQUEST, "반복 예약은 12주 이내의 기간만 가능합니다."),
    INVALID_WEEKDAYS(HttpStatus.BAD_REQUEST, "반복 요일이 올바르지 않습니다."),
    INVALID_HEADCOUNT(HttpStatus.BAD_REQUEST, "이용 인원이 올바르지 않습니다."),
    OVERLAPPING_RESERVATION(HttpStatus.CONFLICT, "해당 시간에 이미 예약이 존재합니다."),
    DAILY_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "하루 이용 가능 시간(3시간)을 초과했습니다."),
    ALREADY_CANCELLED(HttpStatus.CONFLICT, "이미 취소된 예약입니다."),
    CANCEL_DEADLINE_PASSED(HttpStatus.CONFLICT, "이미 시작된 예약은 취소할 수 없습니다."),
    NOT_RESERVATION_OWNER(HttpStatus.FORBIDDEN, "본인의 예약만 조회하거나 취소할 수 있습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    ReservationExceptionType(HttpStatus status, String message) {
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
        return new DetailedReservationExceptionType(this, detailMessage);
    }

    private record DetailedReservationExceptionType(ReservationExceptionType type, String detailMessage)
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
            return new DetailedReservationExceptionType(type, detailMessage);
        }
    }
}
