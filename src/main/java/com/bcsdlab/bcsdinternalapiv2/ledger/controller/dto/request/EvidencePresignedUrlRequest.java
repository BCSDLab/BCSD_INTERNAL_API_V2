package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.EvidenceFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 장부 증빙 업로드 URL 발급. 기존 미디어 API의 요청(png/jpg/webp/svg, 5MB)과 규칙이 달라 따로 둔다.
 * 확장자와 형식은 짝이 맞아야 한다(예: .pdf ↔ application/pdf, .jpg ↔ image/jpeg).
 */
public record EvidencePresignedUrlRequest(
        @NotBlank(message = "파일 이름을 입력하세요.")
        @Size(max = 255, message = "파일 이름은 255자 이하여야 합니다.")
        @Pattern(regexp = "(?i).+\\.(png|jpe?g|webp|pdf)$",
                message = "증빙은 PNG, JPG, WEBP, PDF 파일만 올릴 수 있습니다.")
        String fileName,

        @NotBlank(message = "파일 형식을 입력하세요.")
        @Pattern(regexp = "image/png|image/jpeg|image/webp|application/pdf",
                message = "증빙은 PNG, JPG, WEBP, PDF 파일만 올릴 수 있습니다.")
        String contentType,

        @NotNull(message = "파일 크기를 입력하세요.")
        @Min(value = 1, message = "빈 파일은 올릴 수 없습니다.")
        @Max(value = EvidenceFormat.MAX_BYTES, message = "증빙은 10MB 이하여야 합니다.")
        Long size
) {

    @Schema(hidden = true)
    @AssertTrue(message = "파일 확장자와 형식이 맞지 않습니다.")
    public boolean isExtensionMatchingContentType() {
        if (fileName == null || contentType == null) {
            return true;
        }
        return EvidenceFormat.ofContentType(contentType)
                .map(format -> format.acceptsFileName(fileName))
                .orElse(true); // 허용하지 않는 형식은 contentType 검증이 따로 거부한다.
    }
}
