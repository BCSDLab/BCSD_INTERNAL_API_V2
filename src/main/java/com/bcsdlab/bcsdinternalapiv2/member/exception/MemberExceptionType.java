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
    SLACK_ID_DUPLICATED(HttpStatus.CONFLICT, "이미 다른 회원이 사용 중인 Slack ID입니다."),
    SLACK_ID_MISSING(HttpStatus.BAD_REQUEST, "Slack ID가 등록되지 않은 회원입니다."),
    SLACK_DM_FAILED(HttpStatus.BAD_GATEWAY, "Slack DM 발송에 실패했습니다."),
    SLACK_DM_USER_UNREACHABLE(HttpStatus.BAD_GATEWAY,
            "Slack에서 이 회원에게 DM을 보낼 수 없습니다. Slack ID가 맞는지, 워크스페이스를 떠난 계정은 아닌지 확인하세요."),
    SLACK_DM_APP_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "삐봇의 메시지 탭이 꺼져 있어 DM을 보낼 수 없습니다. Slack 앱 설정을 확인하세요."),
    SLACK_DM_RATE_LIMITED(HttpStatus.BAD_GATEWAY, "Slack 요청이 많아 보내지 못했습니다. 잠시 후 다시 시도하세요."),
    INITIAL_SETUP_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "새 비밀번호를 입력해 주세요."),
    MEMBER_WITHDRAWN(HttpStatus.CONFLICT, "탈퇴한 회원입니다."),
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
