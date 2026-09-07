package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * WHAT WE STUDY 카드 전체 교체. 배열 순서가 곧 display_order다(INV-14: 최대 4개).
 */
public record StudyPointsReplaceRequest(
        @NotNull
        @Size(max = 4, message = "WHAT WE STUDY 카드는 최대 4개까지 등록할 수 있습니다.")
        @Valid
        List<StudyPointRequest> studyPoints
) {
}
