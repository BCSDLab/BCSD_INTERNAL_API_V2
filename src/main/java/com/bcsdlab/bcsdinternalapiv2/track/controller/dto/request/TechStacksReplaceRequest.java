package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 트랙 페이지에 부착할 기술스택 전체 교체. 배열 순서가 곧 display_order다.
 */
public record TechStacksReplaceRequest(
        @NotNull List<Long> techStackIds
) {
}
