package com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** {@code googleFormUrl}은 forms.gle 또는 docs.google.com/forms 호스트만 허용한다(AC-10.6). */
public record RecruitLinkUpdateRequest(
        @NotBlank
        @Pattern(regexp = "^https://(forms\\.gle/.+|docs\\.google\\.com/forms/.+)$",
                message = "forms.gle 또는 docs.google.com/forms 주소만 등록할 수 있습니다.")
        String googleFormUrl,

        boolean isOpen,

        LocalDate closeDate,

        @NotBlank
        @Size(max = 100)
        String closedMessage
) {
}
