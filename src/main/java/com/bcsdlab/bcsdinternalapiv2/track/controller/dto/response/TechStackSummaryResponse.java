package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.track.model.TechStack;

public record TechStackSummaryResponse(
        String name,
        String iconUrl
) {
    public static TechStackSummaryResponse from(TechStack techStack) {
        return new TechStackSummaryResponse(techStack.getName(), techStack.getIconUrl());
    }
}
