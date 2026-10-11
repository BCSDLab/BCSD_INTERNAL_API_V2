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
    ROSTER_MEMBER_HAS_LINKS(HttpStatus.CONFLICT, "연결된 입출금 내역이 있어 납부 비대상으로 바꿀 수 없습니다. 연결을 먼저 해제하세요."),
    ROSTER_MEMBER_NOT_APPLICABLE(HttpStatus.CONFLICT, "이 학기 납부 비대상 회원에게는 입출금 내역을 연결할 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    ENTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "장부 기록을 찾을 수 없습니다."),
    ENTRY_ALREADY_LINKED(HttpStatus.CONFLICT, "이미 회비에 연결된 장부 기록입니다. 다시 시도하세요."),
    DUPLICATED_ENTRY_IN_REQUEST(HttpStatus.BAD_REQUEST, "같은 장부 기록이 요청에 두 번 있습니다."),
    EVIDENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "증빙을 찾을 수 없습니다."),
    EVIDENCE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "업로드가 끝나지 않았거나 다른 기록에 붙은 증빙입니다."),
    EVIDENCE_NOT_UPLOADED(HttpStatus.BAD_REQUEST, "증빙 파일이 업로드되지 않았거나 신청한 형식·크기와 다릅니다."),
    EVIDENCE_VERIFY_FORBIDDEN(HttpStatus.BAD_GATEWAY,
            "서버에 증빙 업로드를 확인할 권한이 없어 완료하지 못했습니다. 관리자에게 문의하세요."),
    EVIDENCE_VERIFY_FAILED(HttpStatus.BAD_GATEWAY, "증빙 업로드를 확인하지 못했습니다. 잠시 뒤 다시 시도하세요."),
    EXEMPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "면제 기록을 찾을 수 없습니다."),
    INVALID_EXEMPTION_PERIOD(HttpStatus.BAD_REQUEST, "종료 월은 시작 월보다 빠를 수 없습니다."),
    IMPORT_FILE_INVALID(HttpStatus.BAD_REQUEST, "신한은행 거래내역 파일을 읽을 수 없습니다."),
    IMPORT_FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "거래내역 파일이 너무 큽니다. 1MB 이하 파일만 올릴 수 있습니다."),
    IMPORT_ROW_KEY_MISMATCH(HttpStatus.BAD_REQUEST, "거래 정보가 미리보기 결과와 다릅니다. 파일을 다시 미리보기 하세요."),
    DUPLICATED_EVIDENCE_IN_REQUEST(HttpStatus.BAD_REQUEST, "같은 증빙이 요청에 두 번 있습니다."),
    DUPLICATED_ROW_KEY_IN_REQUEST(HttpStatus.BAD_REQUEST, "같은 거래가 요청에 두 번 있습니다. 파일을 다시 미리보기 하세요."),
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
