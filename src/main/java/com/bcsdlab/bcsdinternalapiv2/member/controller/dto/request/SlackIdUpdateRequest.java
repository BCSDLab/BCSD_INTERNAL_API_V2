package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request;

import jakarta.validation.constraints.Pattern;

/**
 * null이면 Slack ID를 지운다. Slack 회원 ID는 길이가 고정이 아니다 — 오래된 워크스페이스는 9자(U024BE7LH),
 * Enterprise Grid 계정은 W로 시작한다. 상한 20자는 member.slack_id VARCHAR(20)에 맞췄다.
 */
public record SlackIdUpdateRequest(
        @Pattern(regexp = "^[UW][A-Z0-9]{8,19}$", message = "Slack ID는 U 또는 W로 시작하는 영문 대문자·숫자여야 합니다.")
        String slackId
) {
}
