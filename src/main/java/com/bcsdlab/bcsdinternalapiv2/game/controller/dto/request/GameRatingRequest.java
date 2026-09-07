package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request;

import com.bcsdlab.bcsdinternalapiv2.game.model.GameRatingLevel;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 등급정보 upsert(FR-7.5). {@code contentDescriptors}는
 * {@code sexuality|violence|fear|language|drugs|crime|gambling} 7개 키만 허용한다(INV-21).
 */
public record GameRatingRequest(
        @NotNull GameRatingLevel rating,
        String classificationNumber,
        LocalDate classificationDate,
        String businessName,
        String developerReportNumber,
        List<String> contentDescriptors
) {
}
