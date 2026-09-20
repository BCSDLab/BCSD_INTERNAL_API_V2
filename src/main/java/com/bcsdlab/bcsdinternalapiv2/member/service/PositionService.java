package com.bcsdlab.bcsdinternalapiv2.member.service;

import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.PositionResponse;
import com.bcsdlab.bcsdinternalapiv2.member.repository.PositionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionService {

    private final PositionRepository positionRepository;

    public List<PositionResponse> getPositions() {
        return positionRepository.findAllByActiveTrueOrderByIdAsc().stream()
                .map(PositionResponse::from)
                .toList();
    }
}
