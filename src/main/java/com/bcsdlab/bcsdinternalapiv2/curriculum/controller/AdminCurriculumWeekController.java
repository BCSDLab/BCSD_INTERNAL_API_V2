package com.bcsdlab.bcsdinternalapiv2.curriculum.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.security.LoginMemberId;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.WeekRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumWeekResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.service.AdminCurriculumTreeService;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/curriculums/{curriculumId}/weeks")
@RequiredArgsConstructor
public class AdminCurriculumWeekController implements AdminCurriculumWeekApi {

    private final AdminCurriculumTreeService adminCurriculumTreeService;

    @Override
    @PostMapping
    public ResponseEntity<CurriculumWeekResponse> createWeek(@PathVariable Long curriculumId,
                                                              @Valid @RequestBody WeekRequest request,
                                                              @LoginMemberId Long loginMemberId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCurriculumTreeService.createWeek(curriculumId, request, loginMemberId));
    }

    @Override
    @PatchMapping("/order")
    public ResponseEntity<Void> reorderWeeks(@PathVariable Long curriculumId, @Valid @RequestBody OrderRequest request,
                                              @LoginMemberId Long loginMemberId) {
        adminCurriculumTreeService.reorderWeeks(curriculumId, request, loginMemberId);
        return ResponseEntity.noContent().build();
    }
}
