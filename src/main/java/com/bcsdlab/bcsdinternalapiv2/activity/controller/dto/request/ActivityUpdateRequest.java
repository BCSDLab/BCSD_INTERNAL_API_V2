package com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * categoryId는 수정 대상이 아니다 — 카테고리를 옮기는 요구가 없고, 잘못 만들었으면
 * 삭제 후 다시 만드는 편이 트리(사진 포함)를 옮기는 것보다 단순하다.
 */
public record ActivityUpdateRequest(
        @NotNull
        @Min(2000)
        @Max(2100)
        Integer year,

        @NotNull
        @Min(1)
        @Max(12)
        Integer month,

        @NotBlank
        @Size(max = 80)
        String title,

        @NotBlank
        @Size(max = 200)
        String summary,

        String content,

        String externalUrl
) {
}
