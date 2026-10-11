package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.media.model.ImageAsset;

/** 장부 증빙. name은 올릴 때의 파일 이름, url은 공개 CDN 주소다. width·height는 늘 null이다. */
public record EvidenceResponse(
        Long id,
        String name,
        String url,
        Integer width,
        Integer height
) {

    public static EvidenceResponse from(ImageAsset asset) {
        return new EvidenceResponse(asset.getId(), asset.getOriginalName(), asset.getUrl(), null, null);
    }
}
