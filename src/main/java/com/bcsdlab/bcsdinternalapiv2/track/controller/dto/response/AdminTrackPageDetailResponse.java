package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;

/**
 * 편집 화면 헤더 탭. studyPoints(T-08)·techStacks(T-09)·members(T-18)는 이 레코드에
 * 아직 없다 — 해당 도메인 티켓이 필드를 추가한다.
 */
public record AdminTrackPageDetailResponse(
        Long id,
        Long trackId,
        String trackCode,
        String slug,
        String displayName,
        String tagline,
        String heroImageUrl,
        String ogImageUrl,
        String seoDescription,
        boolean isPublished,
        int displayOrder
) {
    public static AdminTrackPageDetailResponse from(TrackPage trackPage) {
        return new AdminTrackPageDetailResponse(
                trackPage.getId(),
                trackPage.getTrack().getId(),
                trackPage.getTrack().getCode(),
                trackPage.getSlug(),
                trackPage.getDisplayName(),
                trackPage.getTagline(),
                trackPage.getHeroImageUrl(),
                trackPage.getOgImageUrl(),
                trackPage.getSeoDescription(),
                trackPage.isPublished(),
                trackPage.getDisplayOrder());
    }
}
