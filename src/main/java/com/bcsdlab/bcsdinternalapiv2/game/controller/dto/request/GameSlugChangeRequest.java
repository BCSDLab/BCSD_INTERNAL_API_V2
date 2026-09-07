package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record GameSlugChangeRequest(
        @NotBlank
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "slug는 소문자·숫자·하이픈만 사용할 수 있습니다.")
        String slug
) {
}
