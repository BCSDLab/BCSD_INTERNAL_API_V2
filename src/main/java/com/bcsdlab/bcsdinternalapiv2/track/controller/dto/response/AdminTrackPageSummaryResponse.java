package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;

/**
 * 트랙 목록 화면용 요약. 멤버 수는 아직 없다 — track_page_member가 구현되는 T-18에서
 * 이 레코드에 필드를 추가한다.
 */
public record AdminTrackPageSummaryResponse(
        Long id,
        String slug,
        String displayName,
        boolean isPublished,
        int displayOrder
) {
    public static AdminTrackPageSummaryResponse from(TrackPage trackPage) {
        return new AdminTrackPageSummaryResponse(
                trackPage.getId(), trackPage.getSlug(), trackPage.getDisplayName(),
                trackPage.isPublished(), trackPage.getDisplayOrder());
    }
}
