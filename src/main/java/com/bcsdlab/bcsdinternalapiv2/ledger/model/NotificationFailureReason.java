package com.bcsdlab.bcsdinternalapiv2.ledger.model;

/** 회원별 발송 실패 이유. SLACK_ERROR는 Slack 오류·봇 토큰 없음·rate limit 소진·시간 예산 초과를 모두 포함한다. */
public enum NotificationFailureReason {
    SLACK_ID_MISSING,
    SLACK_ERROR
}
