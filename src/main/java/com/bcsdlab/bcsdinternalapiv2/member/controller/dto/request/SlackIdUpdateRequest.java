package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request;

import jakarta.validation.constraints.Pattern;

/** null이면 Slack ID를 지운다. */
public record SlackIdUpdateRequest(
        @Pattern(regexp = "^U[A-Z0-9]{10}$", message = "Slack ID는 U로 시작하는 영문 대문자·숫자 11자리여야 합니다.")
        String slackId
) {
}
