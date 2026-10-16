package com.bcsdlab.bcsdinternalapiv2.curriculum.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.security.LoginMemberId;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.TopicRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.WeekRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumTopicResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumWeekResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.service.AdminCurriculumTreeService;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/weeks")
@RequiredArgsConstructor
public class AdminWeekController implements AdminWeekApi {

    private final AdminCurriculumTreeService adminCurriculumTreeService;

    @Override
    @PutMapping("/{id}")
    public CurriculumWeekResponse updateWeek(@PathVariable Long id, @Valid @RequestBody WeekRequest request,
                                              @LoginMemberId Long loginMemberId) {
        return adminCurriculumTreeService.updateWeek(id, request, loginMemberId);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWeek(@PathVariable Long id, @LoginMemberId Long loginMemberId) {
        adminCurriculumTreeService.deleteWeek(id, loginMemberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{id}/topics")
    public ResponseEntity<CurriculumTopicResponse> createTopic(@PathVariable Long id,
                                                                @Valid @RequestBody TopicRequest request,
                                                                @LoginMemberId Long loginMemberId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCurriculumTreeService.createTopic(id, request, loginMemberId));
    }

    @Override
    @PatchMapping("/{id}/topics/order")
    public ResponseEntity<Void> reorderTopics(@PathVariable Long id, @Valid @RequestBody OrderRequest request,
                                               @LoginMemberId Long loginMemberId) {
        adminCurriculumTreeService.reorderTopics(id, request, loginMemberId);
        return ResponseEntity.noContent().build();
    }
}
