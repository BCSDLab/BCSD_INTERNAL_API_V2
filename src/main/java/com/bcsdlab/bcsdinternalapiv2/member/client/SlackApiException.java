package com.bcsdlab.bcsdinternalapiv2.member.client;

/**
 * Slack API 호출 실패. error는 Slack 오류 코드(ok=false의 error) 또는 "http_{상태}"다.
 * 429(rate limit)면 retryAfterSeconds에 Retry-After 값이 들어간다.
 */
public class SlackApiException extends RuntimeException {

    private final String error;
    private final Long retryAfterSeconds;

    public SlackApiException(String error, Long retryAfterSeconds) {
        super("Slack API error: " + error);
        this.error = error;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public String getError() {
        return error;
    }

    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public boolean isRateLimited() {
        return retryAfterSeconds != null;
    }
}
