package com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response;

/** accountSetup: 초기 설정 전 회원(가입 전)의 링크인지. 화면 문구를 "계정 비밀번호 설정"으로 바꾸는 데 쓴다. */
public record ResetTokenValidationResponse(boolean valid, String studentNumberMasked, boolean accountSetup) {
}
