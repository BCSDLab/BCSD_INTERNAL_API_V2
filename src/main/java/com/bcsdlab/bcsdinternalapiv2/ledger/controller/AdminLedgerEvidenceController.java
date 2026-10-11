package com.bcsdlab.bcsdinternalapiv2.ledger.controller;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.EvidencePresignedUrlRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.EvidencePresignedUrlResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.EvidenceResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.EvidenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/ledger/evidences")
@RequiredArgsConstructor
public class AdminLedgerEvidenceController implements AdminLedgerEvidenceApi {

    private final EvidenceService evidenceService;

    @Override
    @PostMapping("/presigned-url")
    public ResponseEntity<EvidencePresignedUrlResponse> issuePresignedUrl(
            @Valid @RequestBody EvidencePresignedUrlRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(evidenceService.issue(request, Long.valueOf(jwt.getSubject())));
    }

    @Override
    @PostMapping("/{id}/complete")
    public ResponseEntity<EvidenceResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(evidenceService.complete(id));
    }
}
