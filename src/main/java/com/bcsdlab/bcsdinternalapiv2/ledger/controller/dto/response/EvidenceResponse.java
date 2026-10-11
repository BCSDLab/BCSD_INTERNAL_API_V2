package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

/** 장부 증빙. width·height는 늘 null이다(증빙 업로드는 PR3). */
public record EvidenceResponse(
        Long id,
        String name,
        String url,
        Integer width,
        Integer height
) {
}
