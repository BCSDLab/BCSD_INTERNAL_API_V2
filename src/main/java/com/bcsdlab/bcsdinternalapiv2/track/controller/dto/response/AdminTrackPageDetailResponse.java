package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import java.util.List;

/**
 * 편집 화면 집계(05-api-spec.md — "헤더+studyPoints+techStacks+members"). {@code from}은
 * 헤더만 바꾸는 응답(생성·헤더 수정·slug 변경)에 쓰고 나머지 목록은 빈 배열로 둔다 — 그
 * 호출들은 study point/기술스택/멤버 상태를 프런트가 이미 들고 있어 다시 받을 필요가
 * 없다. {@code of}는 전체 편집 화면을 처음 여는 GET에서만 쓴다.
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
        int displayOrder,
        List<StudyPointResponse> studyPoints,
        List<TechStackResponse> techStacks,
        List<AdminTrackPageMemberResponse> members
) {
    public static AdminTrackPageDetailResponse from(TrackPage trackPage) {
        return of(trackPage, List.of(), List.of(), List.of());
    }

    public static AdminTrackPageDetailResponse of(TrackPage trackPage, List<StudyPointResponse> studyPoints,
                                                    List<TechStackResponse> techStacks,
                                                    List<AdminTrackPageMemberResponse> members) {
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
                trackPage.getDisplayOrder(),
                studyPoints,
                techStacks,
                members);
    }
}
