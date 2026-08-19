package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;

/**
 * T-06은 헤더 필드만 채운다. studyPoints(T-08)·techStacks(T-09)·curriculum(T-12)·
 * members(T-18)는 각 도메인 티켓이 이 레코드에 필드를 추가하는 방식으로 확장한다 —
 * 지금 존재하지 않는 데이터를 위해 빈 자리표시자 구조를 미리 만들지 않는다.
 */
public record TrackDetailResponse(
        String slug,
        String name,
        String tagline,
        String heroImageUrl,
        String ogImageUrl,
        String seoDescription
) {
    public static TrackDetailResponse from(TrackPage trackPage) {
        return new TrackDetailResponse(
                trackPage.getSlug(),
                trackPage.getDisplayName(),
                trackPage.getTagline(),
                trackPage.getHeroImageUrl(),
                trackPage.getOgImageUrl(),
                trackPage.getSeoDescription()
        );
    }
}
