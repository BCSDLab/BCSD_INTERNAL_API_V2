package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntryEvidenceLink;

/** 장부 증빙 링크. label은 없으면 null이다. */
public record EvidenceResponse(
        Long id,
        String url,
        String label
) {

    public static EvidenceResponse from(LedgerEntryEvidenceLink link) {
        return new EvidenceResponse(link.getId(), link.getUrl(), link.getLabel());
    }
}
