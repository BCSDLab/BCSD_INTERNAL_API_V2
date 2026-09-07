package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumResponse;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import java.util.List;

public record TrackDetailResponse(
        String slug,
        String name,
        String tagline,
        List<StudyPointResponse> studyPoints,
        List<TechStackSummaryResponse> techStacks,
        CurriculumResponse curriculum,
        List<TrackMemberResponse> members
) {
    public static TrackDetailResponse of(TrackPage trackPage, List<StudyPointResponse> studyPoints,
                                          List<TechStackSummaryResponse> techStacks, CurriculumResponse curriculum,
                                          List<TrackMemberResponse> members) {
        return new TrackDetailResponse(
                trackPage.getSlug(),
                trackPage.getDisplayName(),
                trackPage.getTagline(),
                studyPoints,
                techStacks,
                curriculum,
                members
        );
    }
}
