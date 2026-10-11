package com.bcsdlab.bcsdinternalapiv2.member.client;

/** 보낸 메시지의 위치. channel은 봇과의 DM(D…), ts는 메시지 id다(나중에 수정·삭제할 때 쓴다). */
public record SlackPostResult(String channel, String ts) {
}
