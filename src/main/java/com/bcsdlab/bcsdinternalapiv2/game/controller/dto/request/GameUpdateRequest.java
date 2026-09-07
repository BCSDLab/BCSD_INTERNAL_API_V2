package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 상세 설명(description)은 저장 시 {@code ActivityContentSanitizer}로 정제된다(ADR-008,
 * INV-9, INV-10). 길이 제한을 두지 않는다 — TEXT 컬럼이고 활동 본문과 같은 규약이다.
 */
public record GameUpdateRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 500)
        String oneLiner,

        Long trackId,

        @Size(max = 30)
        String teamLabel,

        String description
) {
}
