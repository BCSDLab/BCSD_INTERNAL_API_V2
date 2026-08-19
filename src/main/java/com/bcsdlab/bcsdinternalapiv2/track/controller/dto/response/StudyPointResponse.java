package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.track.model.TrackStudyPoint;

public record StudyPointResponse(
        String title,
        String description,
        String iconImageUrl
) {
    public static StudyPointResponse from(TrackStudyPoint studyPoint) {
        return new StudyPointResponse(studyPoint.getTitle(), studyPoint.getDescription(),
                studyPoint.getIconImageUrl());
    }
}
