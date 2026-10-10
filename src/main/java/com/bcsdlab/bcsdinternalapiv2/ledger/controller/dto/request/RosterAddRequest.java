package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request;

import jakarta.validation.constraints.NotNull;

public record RosterAddRequest(
        @NotNull(message = "회원을 선택하세요.")
        Long memberId,

        @NotNull(message = "납부 대상 여부를 입력하세요.")
        Boolean applicable
) {
}
