package com.bcsdlab.bcsdinternalapiv2.member.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import org.springframework.http.HttpStatus;

public enum MemberExceptionType implements BcsdExceptionType {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    ALREADY_ACTIVATED(HttpStatus.CONFLICT, "이미 초기 설정이 완료된 계정입니다."),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    STUDENT_NUMBER_DUPLICATED(HttpStatus.CONFLICT, "이미 등록된 학번입니다."),
    INVALID_PHONE(HttpStatus.BAD_REQUEST, "올바른 전화번호 형식이 아닙니다."),
    INVALID_GITHUB_ID(HttpStatus.BAD_REQUEST, "올바른 깃허브 아이디가 아닙니다."),
    INVALID_PHOTO(HttpStatus.BAD_REQUEST, "올바른 사진 파일이 아닙니다."),
    POSITION_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 직책입니다."),
    SLACK_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "Slack에서 해당 이메일로 등록된 프로필 이미지를 찾을 수 없습니다."),
    SLACK_SYNC_FAILED(HttpStatus.BAD_GATEWAY, "Slack 연동 중 오류가 발생했습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    MemberExceptionType(HttpStatus status, String message) {
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
        return new DetailedMemberExceptionType(this, detailMessage);
    }

    private record DetailedMemberExceptionType(MemberExceptionType type, String detailMessage)
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
            return new DetailedMemberExceptionType(type, detailMessage);
        }
    }
}
