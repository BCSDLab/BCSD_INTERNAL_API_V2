package com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request;

import jakarta.validation.constraints.Size;

/**
 * sourceCurriculumId를 주면 그 세트를 트리 전체(주차·토픽·세부항목)까지 복제한다(T-13,
 * FR-2.9). 그 경우 name을 비워 두면 원본 이름을 그대로 쓴다 — 복제 직후 화면에서 이름을
 * 바꿀 수 있으므로 별도 입력을 강제하지 않는다. sourceCurriculumId가 없으면 name이
 * 필수다(서비스 레이어에서 검증).
 */
public record CurriculumCreateRequest(
        @Size(max = 50)
        String name,

        Long sourceCurriculumId
) {
    public boolean isClone() {
        return sourceCurriculumId != null;
    }
}
