package com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 활동 사진 전체 교체. 배열의 첫 번째가 목록 썸네일이다(INV-12).
 */
public record ActivityImagesReplaceRequest(
        @NotNull
        List<@NotBlank String> imageUrls
) {
}
