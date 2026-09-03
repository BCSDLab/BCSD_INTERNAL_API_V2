package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/** 스크린샷 전체 교체(FR-7.4). 배열 순서가 display_order다. */
public record GameScreenshotsReplaceRequest(
        @NotNull List<String> imageUrls
) {
}
