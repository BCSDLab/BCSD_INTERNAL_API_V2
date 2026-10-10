package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request;

import jakarta.validation.constraints.NotNull;

public record RosterUpdateRequest(
        @NotNull(message = "납부 대상 여부를 입력하세요.")
        Boolean applicable
) {
}
