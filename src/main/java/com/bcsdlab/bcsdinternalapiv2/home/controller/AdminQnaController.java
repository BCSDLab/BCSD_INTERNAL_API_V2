package com.bcsdlab.bcsdinternalapiv2.home.controller;

import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request.QnaCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request.QnaUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.AdminQnaResponse;
import com.bcsdlab.bcsdinternalapiv2.home.service.AdminQnaService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/qna")
@RequiredArgsConstructor
public class AdminQnaController implements AdminQnaApi {

    private final AdminQnaService adminQnaService;

    @Override
    @GetMapping
    public List<AdminQnaResponse> getQnaItems() {
        return adminQnaService.getQnaItems();
    }

    @Override
    @PostMapping
    public ResponseEntity<AdminQnaResponse> createQnaItem(@Valid @RequestBody QnaCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminQnaService.createQnaItem(request));
    }

    @Override
    @PutMapping("/{id}")
    public AdminQnaResponse updateQnaItem(@PathVariable Long id, @Valid @RequestBody QnaUpdateRequest request) {
        return adminQnaService.updateQnaItem(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQnaItem(@PathVariable Long id) {
        adminQnaService.deleteQnaItem(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id, @Valid @RequestBody PublishRequest request) {
        adminQnaService.publish(id, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/order")
    public ResponseEntity<Void> reorder(@Valid @RequestBody OrderRequest request) {
        adminQnaService.reorder(request);
        return ResponseEntity.noContent().build();
    }
}
