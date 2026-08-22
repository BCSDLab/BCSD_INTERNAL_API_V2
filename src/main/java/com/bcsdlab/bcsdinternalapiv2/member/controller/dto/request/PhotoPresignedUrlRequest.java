package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PhotoPresignedUrlRequest(
        @NotBlank
        @Pattern(regexp = "(?i).+\\.(png|jpe?g|webp)$", message = "허용되지 않는 확장자입니다.")
        String fileName,

        @NotBlank
        String contentType,

        @NotNull
        @Max(5L * 1024 * 1024)
        Long byteSize
) {
}
