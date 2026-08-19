package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumResponse;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import java.util.List;

/**
 * members(T-18)는 T-18이 이 레코드에 필드를 추가하는 방식으로 확장한다 — 지금 존재하지
 * 않는 데이터를 위해 빈 자리표시자 구조를 미리 만들지 않는다.
 */
public record TrackDetailResponse(
        String slug,
        String name,
        String tagline,
        String heroImageUrl,
        String ogImageUrl,
        String seoDescription,
        List<StudyPointResponse> studyPoints,
        List<TechStackSummaryResponse> techStacks,
        CurriculumResponse curriculum
) {
    public static TrackDetailResponse of(TrackPage trackPage, List<StudyPointResponse> studyPoints,
                                          List<TechStackSummaryResponse> techStacks, CurriculumResponse curriculum) {
        return new TrackDetailResponse(
                trackPage.getSlug(),
                trackPage.getDisplayName(),
                trackPage.getTagline(),
                trackPage.getHeroImageUrl(),
                trackPage.getOgImageUrl(),
                trackPage.getSeoDescription(),
                studyPoints,
                techStacks,
                curriculum
        );
    }
}
