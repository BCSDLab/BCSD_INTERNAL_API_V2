package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 단건 연결(입금·출금 공통). 이미 연결돼 있으면 연결 대상을 바꾼다. */
public record DuesLinkRequest(
        @NotNull(message = "회원을 선택하세요.")
        Long memberId,

        @NotBlank(message = "학기를 선택하세요.")
        String semesterId
) {
}
