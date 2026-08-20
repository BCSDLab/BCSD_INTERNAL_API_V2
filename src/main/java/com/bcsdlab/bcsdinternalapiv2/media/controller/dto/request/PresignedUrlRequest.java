package com.bcsdlab.bcsdinternalapiv2.media.controller.dto.request;

import com.bcsdlab.bcsdinternalapiv2.media.model.ImagePurpose;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 확장자·용량 검증은 여기서 끝낸다(AC-4.1, AC-4.2). 서비스는 값을 그대로 신뢰한다.
 */
public record PresignedUrlRequest(
        @NotBlank
        @Pattern(regexp = "(?i).+\\.(png|jpe?g|webp|svg)$", message = "허용되지 않는 확장자입니다.")
        String fileName,

        @NotBlank
        String contentType,

        @NotNull
        @Max(5L * 1024 * 1024)
        Long byteSize,

        @NotNull
        ImagePurpose purpose
) {
}
