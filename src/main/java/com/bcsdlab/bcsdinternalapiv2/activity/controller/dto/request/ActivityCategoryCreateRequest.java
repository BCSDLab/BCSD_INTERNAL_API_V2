package com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActivityCategoryCreateRequest(
        @NotBlank
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "slug는 소문자·숫자·하이픈만 사용할 수 있습니다.")
        String slug,

        @NotBlank
        @Size(max = 30)
        String name,

        @Size(max = 200)
        String headline,

        String heroImageUrl
) {
}
