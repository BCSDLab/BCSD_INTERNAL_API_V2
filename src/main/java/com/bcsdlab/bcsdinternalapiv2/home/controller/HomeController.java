package com.bcsdlab.bcsdinternalapiv2.home.controller;

import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.HomeResponse;
import com.bcsdlab.bcsdinternalapiv2.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/home")
@RequiredArgsConstructor
public class HomeController implements HomeApi {

    private final HomeService homeService;

    @Override
    @GetMapping
    public ResponseEntity<HomeResponse> getHome() {
        return ResponseEntity.ok(homeService.getHome());
    }
}
