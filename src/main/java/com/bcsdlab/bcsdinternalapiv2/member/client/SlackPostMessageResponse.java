package com.bcsdlab.bcsdinternalapiv2.member.client;

/** chat.postMessage 응답. ok=false면 error에 Slack 오류 코드(missing_scope, channel_not_found 등)가 온다. */
record SlackPostMessageResponse(boolean ok, String error, String channel, String ts) {
}
