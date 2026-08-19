package com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * sourceCurriculumId는 T-13(세트 복제)에서 추가된다. 지금은 순수 생성만 지원한다.
 */
public record CurriculumCreateRequest(
        @NotBlank
        @Size(max = 50)
        String name
) {
}
