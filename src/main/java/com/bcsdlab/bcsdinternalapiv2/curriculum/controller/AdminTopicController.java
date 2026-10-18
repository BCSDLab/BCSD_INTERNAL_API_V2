package com.bcsdlab.bcsdinternalapiv2.curriculum.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.security.LoginMemberId;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.TopicDetailsReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.TopicRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumTopicResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.service.AdminCurriculumTreeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/topics")
@RequiredArgsConstructor
public class AdminTopicController implements AdminTopicApi {

    private final AdminCurriculumTreeService adminCurriculumTreeService;

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CurriculumTopicResponse> updateTopic(@PathVariable Long id, @Valid @RequestBody TopicRequest request,
                                                                @LoginMemberId Long loginMemberId) {
        return ResponseEntity.ok(adminCurriculumTreeService.updateTopic(id, request, loginMemberId));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id, @LoginMemberId Long loginMemberId) {
        adminCurriculumTreeService.deleteTopic(id, loginMemberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping("/{id}/details")
    public ResponseEntity<List<String>> replaceDetails(@PathVariable Long id, @Valid @RequestBody TopicDetailsReplaceRequest request,
                                                        @LoginMemberId Long loginMemberId) {
        return ResponseEntity.ok(adminCurriculumTreeService.replaceDetails(id, request, loginMemberId));
    }
}
