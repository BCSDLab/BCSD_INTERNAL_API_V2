package com.bcsdlab.bcsdinternalapiv2.ledger.controller;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.DuesLinkRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.LedgerEntryUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.LedgerEntryListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.LedgerEntryResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesLinkService;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.LedgerEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/ledger/entries")
@RequiredArgsConstructor
public class AdminLedgerController implements AdminLedgerApi {

    private final LedgerEntryService ledgerEntryService;
    private final DuesLinkService duesLinkService;

    @Override
    @GetMapping
    public ResponseEntity<LedgerEntryListResponse> getEntries() {
        return ResponseEntity.ok(ledgerEntryService.getEntries());
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<LedgerEntryResponse> updateEntry(@PathVariable Long id,
                                                           @Valid @RequestBody LedgerEntryUpdateRequest request) {
        return ResponseEntity.ok(ledgerEntryService.update(id, request));
    }

    @Override
    @PutMapping("/{id}/dues-link")
    public ResponseEntity<LedgerEntryResponse> linkEntry(@PathVariable Long id,
                                                         @Valid @RequestBody DuesLinkRequest request,
                                                         @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(duesLinkService.link(id, request, Long.valueOf(jwt.getSubject())));
    }

    @Override
    @DeleteMapping("/{id}/dues-link")
    public ResponseEntity<Void> unlinkEntry(@PathVariable Long id) {
        duesLinkService.unlink(id);
        return ResponseEntity.noContent().build();
    }
}
