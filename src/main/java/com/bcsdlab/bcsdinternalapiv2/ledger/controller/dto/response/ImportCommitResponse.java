package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

/** skippedDuplicateCount는 이미 장부에 있던(rowKey가 같은) 거래 수다. */
public record ImportCommitResponse(
        int createdCount,
        int skippedDuplicateCount
) {
}
