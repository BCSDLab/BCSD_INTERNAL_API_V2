package com.bcsdlab.bcsdinternalapiv2.member.controller;

import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.PositionResponse;
import com.bcsdlab.bcsdinternalapiv2.member.service.PositionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/positions")
@RequiredArgsConstructor
public class PositionController implements PositionApi {

    private final PositionService positionService;

    @Override
    @GetMapping
    public List<PositionResponse> getPositions() {
        return positionService.getPositions();
    }
}
