package com.bcsdlab.bcsdinternalapiv2.homepage.controller;

import com.bcsdlab.bcsdinternalapiv2.homepage.controller.dto.response.HomepageSyncResponse;
import com.bcsdlab.bcsdinternalapiv2.homepage.service.HomepageSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/homepage-sync")
@RequiredArgsConstructor
public class AdminHomepageSyncController implements AdminHomepageSyncApi {

    private final HomepageSyncService homepageSyncService;

    @Override
    @GetMapping
    public HomepageSyncResponse getStatus() {
        return homepageSyncService.getStatus();
    }

    @Override
    @PostMapping
    public void forceResync() {
        homepageSyncService.forceResync();
    }
}
