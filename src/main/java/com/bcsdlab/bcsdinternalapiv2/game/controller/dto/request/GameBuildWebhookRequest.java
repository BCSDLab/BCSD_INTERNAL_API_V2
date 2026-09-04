package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 홈페이지 서버가 업로드 처리 결과를 통지하는 완료 웹훅 요청(ADR-024). {@code status}는
 * {@code ACTIVE} 또는 {@code FAILED}만 허용한다.
 */
public record GameBuildWebhookRequest(
        @NotBlank
        String status,
        Integer canvasWidth,
        Integer canvasHeight,
        Long storageBytes,
        String buildFileUrl,
        String failureReason
) {
}
