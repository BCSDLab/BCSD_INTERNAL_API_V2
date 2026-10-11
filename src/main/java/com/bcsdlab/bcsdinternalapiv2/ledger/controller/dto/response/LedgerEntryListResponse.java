package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import java.util.List;

public record LedgerEntryListResponse(
        List<LedgerEntryResponse> entries
) {
}
