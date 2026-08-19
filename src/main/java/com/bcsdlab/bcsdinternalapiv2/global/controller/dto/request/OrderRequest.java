package com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 순서 변경 전용 규약: {@code PATCH .../order}. 배열 인덱스가 새 display_order다.
 * {@link com.bcsdlab.bcsdinternalapiv2.global.util.DisplayOrders}와 짝을 이룬다.
 */
public record OrderRequest(@NotNull List<Long> ids) {
}
