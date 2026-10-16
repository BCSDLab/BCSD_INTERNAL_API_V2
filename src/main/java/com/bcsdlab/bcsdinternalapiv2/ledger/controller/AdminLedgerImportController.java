package com.bcsdlab.bcsdinternalapiv2.ledger.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.security.LoginMemberId;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.ImportCommitRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ImportCommitResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ImportPreviewResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/admin/ledger/imports")
@RequiredArgsConstructor
public class AdminLedgerImportController implements AdminLedgerImportApi {

    private final ImportService importService;

    @Override
    @PostMapping("/preview")
    public ResponseEntity<ImportPreviewResponse> preview(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(importService.preview(file));
    }

    @Override
    @PostMapping
    public ResponseEntity<ImportCommitResponse> commit(@Valid @RequestBody ImportCommitRequest request,
                                                       @LoginMemberId Long loginMemberId) {
        return ResponseEntity.ok(importService.commit(request, loginMemberId));
    }
}
