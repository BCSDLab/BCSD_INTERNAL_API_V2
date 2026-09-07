package com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 세부 항목 전체 교체. 빈 배열이면 전부 삭제된다(AC-2.9, 에러가 아니다).
 */
public record TopicDetailsReplaceRequest(
        @NotNull
        List<@NotBlank @Size(max = 300) String> contents
) {
}
