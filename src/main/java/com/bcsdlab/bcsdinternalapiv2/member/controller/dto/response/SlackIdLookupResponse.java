package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response;

import java.util.List;

public record SlackIdLookupResponse(List<Result> results) {

    /**
     * @param slackId   SAVED면 저장된 값, DUPLICATED면 찾았지만 저장하지 않은 값, 그 밖에는 null
     * @param ownerName DUPLICATED일 때 그 Slack ID를 이미 쓰고 있는 회원 이름
     */
    public record Result(Long memberId, String slackId, Status status, String ownerName) {

        public static Result of(Long memberId, Status status) {
            return new Result(memberId, null, status, null);
        }
    }

    public enum Status {
        /** 저장됨(이미 값이 있던 경우 포함). */
        SAVED,
        /** Slack 워크스페이스에서 그 이메일을 찾지 못함. */
        NOT_FOUND,
        /** 찾은 Slack ID를 다른 회원이 이미 사용 중이라 저장하지 않음. */
        DUPLICATED,
        /** Slack 호출 실패(토큰·네트워크 등). */
        FAILED,
        MEMBER_NOT_FOUND,
    }
}
