package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 빌드 메타 등록(FR-7.6). 이번 1차는 버전만 받는다 — 실제 ZIP 업로드·압축해제·서빙은
 * 후속 태스크(T-41, ADR-023)에서 다룬다.
 */
public record GameBuildCreateRequest(
        @NotBlank
        @Size(max = 30)
        String version
) {
}
