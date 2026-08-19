package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.track.model.TechStack;

/**
 * 관리자용 — id를 포함해 선택·해제 UI가 특정 항목을 가리킬 수 있게 한다.
 * 공개 응답에는 {@link TechStackSummaryResponse}(id 없음)를 쓴다.
 */
public record TechStackResponse(
        Long id,
        String name,
        String iconUrl
) {
    public static TechStackResponse from(TechStack techStack) {
        return new TechStackResponse(techStack.getId(), techStack.getName(), techStack.getIconUrl());
    }
}
