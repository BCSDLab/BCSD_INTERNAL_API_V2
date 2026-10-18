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
    EXEMPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "면제 기록을 찾을 수 없습니다."),
    INVALID_EXEMPTION_PERIOD(HttpStatus.BAD_REQUEST, "종료 월은 시작 월보다 빠를 수 없습니다."),
    IMPORT_FILE_INVALID(HttpStatus.BAD_REQUEST, "신한은행 거래내역 파일을 읽을 수 없습니다."),
    IMPORT_FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "거래내역 파일이 너무 큽니다. 1MB 이하 파일만 올릴 수 있습니다."),
    IMPORT_ROW_KEY_MISMATCH(HttpStatus.BAD_REQUEST, "거래 정보가 미리보기 결과와 다릅니다. 파일을 다시 미리보기 하세요."),
    EVIDENCE_LINK_NOT_GOOGLE_DRIVE(HttpStatus.BAD_REQUEST, "구글 드라이브 링크만 첨부할 수 있습니다."),
    DUPLICATED_EVIDENCE_LINK_IN_REQUEST(HttpStatus.BAD_REQUEST, "같은 증빙 링크가 두 번 있습니다."),
    DUPLICATED_ROW_KEY_IN_REQUEST(HttpStatus.BAD_REQUEST, "같은 거래가 요청에 두 번 있습니다. 파일을 다시 미리보기 하세요."),
    DUPLICATED_MEMBER_IN_REQUEST(HttpStatus.BAD_REQUEST, "같은 회원이 요청에 두 번 있습니다."),
    SENDER_SLACK_ID_MISSING(HttpStatus.BAD_REQUEST,
            "내 Slack ID가 인명부에 없어 담당자 멘션을 넣을 수 없습니다. 인명부에서 Slack ID를 먼저 저장하세요."),
    TEST_SENDER_SLACK_ID_MISSING(HttpStatus.BAD_REQUEST,
            "내 Slack ID가 인명부에 없어 테스트 발송을 할 수 없습니다. 인명부에서 Slack ID를 먼저 저장하세요."),
    IDEMPOTENCY_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "요청 키(Idempotency-Key)가 없거나 형식이 올바르지 않습니다."),
    IDEMPOTENCY_KEY_REUSED(HttpStatus.CONFLICT, "이미 다른 내용으로 보낸 요청 키입니다. 새로 보내려면 다시 시도하세요."),
    NOTIFICATION_IN_PROGRESS(HttpStatus.CONFLICT, "같은 알림을 보내는 중입니다. 잠시 뒤 결과를 확인하세요."),
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
}
