package com.bcsdlab.bcsdinternalapiv2.ledger.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.security.LoginMemberId;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.ExemptionUpsertRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ExemptionListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ExemptionReasonListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ExemptionResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ExemptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/dues")
@RequiredArgsConstructor
public class AdminDuesExemptionController implements AdminDuesExemptionApi {

    private final ExemptionService exemptionService;

    @Override
    @GetMapping("/exemptions")
    public ResponseEntity<ExemptionListResponse> getExemptions() {
        return ResponseEntity.ok(exemptionService.getExemptions());
    }

    @Override
    @PostMapping("/exemptions")
    public ResponseEntity<ExemptionResponse> createExemption(@Valid @RequestBody ExemptionUpsertRequest request,
                                                             @LoginMemberId Long loginMemberId) {
        ExemptionResponse created = exemptionService.create(request.toDraft(), loginMemberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @PutMapping("/exemptions/{id}")
    public ResponseEntity<ExemptionResponse> updateExemption(@PathVariable Long id,
                                                             @Valid @RequestBody ExemptionUpsertRequest request) {
        return ResponseEntity.ok(exemptionService.update(id, request.toDraft()));
    }

    @Override
    @GetMapping("/exemption-reasons")
    public ResponseEntity<ExemptionReasonListResponse> getReasons() {
        return ResponseEntity.ok(exemptionService.getReasons());
    }
}
