package com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request;

/**
 * 공개/숨김 전용 규약: {@code PATCH .../{id}/publish}. 트랙 페이지·커리큘럼 세트·
 * 활동 카테고리·활동이 전부 이 형태를 공유한다(05-api-spec.md 공통 규약).
 */
public record PublishRequest(boolean isPublished) {
}
