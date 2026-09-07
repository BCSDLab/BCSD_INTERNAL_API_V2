package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.game.model.GameRating;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record GameRatingResponse(
        String rating,
        String classificationNumber,
        LocalDate classificationDate,
        String businessName,
        String developerReportNumber,
        List<String> contentDescriptors
) {
    public static GameRatingResponse from(GameRating gameRating) {
        List<String> descriptors = new ArrayList<>();
        if (gameRating.isDescSexuality()) {
            descriptors.add("sexuality");
        }
        if (gameRating.isDescViolence()) {
            descriptors.add("violence");
        }
        if (gameRating.isDescFear()) {
            descriptors.add("fear");
        }
        if (gameRating.isDescLanguage()) {
            descriptors.add("language");
        }
        if (gameRating.isDescDrugs()) {
            descriptors.add("drugs");
        }
        if (gameRating.isDescCrime()) {
            descriptors.add("crime");
        }
        if (gameRating.isDescGambling()) {
            descriptors.add("gambling");
        }
        return new GameRatingResponse(
                gameRating.getRating().name(),
                gameRating.getClassificationNumber(),
                gameRating.getClassificationDate(),
                gameRating.getBusinessName(),
                gameRating.getDeveloperReportNumber(),
                descriptors
        );
    }
}
