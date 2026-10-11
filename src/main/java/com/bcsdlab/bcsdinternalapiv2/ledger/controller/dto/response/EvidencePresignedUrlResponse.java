package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

/** evidenceId는 image_asset id다. 업로드한 뒤 complete를 부르고, 장부 수정의 evidenceIds로 붙인다. */
public record EvidencePresignedUrlResponse(
        Long evidenceId,
        String uploadUrl
) {
}
