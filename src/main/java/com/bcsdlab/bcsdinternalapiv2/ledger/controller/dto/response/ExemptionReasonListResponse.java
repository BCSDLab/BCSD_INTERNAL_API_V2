package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import java.util.List;

/** 시드 15개 다음 등록 순. */
public record ExemptionReasonListResponse(
        List<String> reasons
) {
}
