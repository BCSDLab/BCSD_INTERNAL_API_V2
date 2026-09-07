package com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 공개/숨김은 별도 PATCH .../publish로만 다룬다(트랙 페이지·커리큘럼과 동일한 규약).
 * 사진은 PUT .../images 전체 교체로만 다룬다 — 항목이 여러 개고 "드래그 후 저장" 화면이라
 * study-points/tech-stacks와 같은 패턴을 쓴다.
 */
public record ActivityCreateRequest(
        @NotNull Long categoryId,

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
