package com.bcsdlab.bcsdinternalapiv2.homepage.controller;

import com.bcsdlab.bcsdinternalapiv2.homepage.controller.dto.response.HomepageSyncResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "홈페이지 반영 상태 (관리자)")
public interface AdminHomepageSyncApi {

    @Operation(summary = "마지막 웹훅 반영 상태 조회")
    HomepageSyncResponse getStatus();

    @Operation(summary = "전체 태그 강제 무효화 (수동 복구)")
    void forceResync();
}
