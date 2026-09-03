package com.bcsdlab.bcsdinternalapiv2.home.controller;

import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request.MentorSlotCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.AdminMentorSlotResponse;
import com.bcsdlab.bcsdinternalapiv2.home.service.AdminMentorSlotService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/mentor-slots")
@RequiredArgsConstructor
public class AdminMentorSlotController implements AdminMentorSlotApi {

    private final AdminMentorSlotService adminMentorSlotService;

    @Override
    @GetMapping
    public List<AdminMentorSlotResponse> getSlots() {
        return adminMentorSlotService.getSlots();
    }

    @Override
    @PostMapping
    public List<AdminMentorSlotResponse> addSlot(@Valid @RequestBody MentorSlotCreateRequest request) {
        return adminMentorSlotService.addSlot(request);
    }

    @Override
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeSlot(@PathVariable Long memberId) {
        adminMentorSlotService.removeSlot(memberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/order")
    public ResponseEntity<Void> reorder(@Valid @RequestBody OrderRequest request) {
        adminMentorSlotService.reorder(request);
        return ResponseEntity.noContent().build();
    }
}
