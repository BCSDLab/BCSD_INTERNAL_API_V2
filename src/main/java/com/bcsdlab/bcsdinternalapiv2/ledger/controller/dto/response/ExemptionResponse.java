package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesExemption;

/** startMonth·endMonth는 "2026-09" 형식이다. endMonth가 null이면 종료 없이 계속이다. */
public record ExemptionResponse(
        Long id,
        Long memberId,
        String reason,
        String note,
        String startMonth,
        String endMonth
) {

    public static ExemptionResponse from(DuesExemption exemption) {
        return new ExemptionResponse(
                exemption.getId(),
                exemption.getMemberId(),
                exemption.getReason(),
                exemption.getNote(),
                exemption.getStartMonth().toString(),
                exemption.getEndMonth() == null ? null : exemption.getEndMonth().toString());
    }
}
