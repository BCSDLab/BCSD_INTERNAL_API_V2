package com.bcsdlab.bcsdinternalapiv2.track.service;

import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TechStackCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TechStackResponse;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackException;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackExceptionType;
import com.bcsdlab.bcsdinternalapiv2.track.model.TechStack;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TechStackRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTechStackService {

    private final TechStackRepository techStackRepository;

    public List<TechStackResponse> getTechStacks() {
        return techStackRepository.findAll().stream()
                .map(TechStackResponse::from)
                .toList();
    }

    @Transactional
    public TechStackResponse createTechStack(TechStackCreateRequest request) {
        if (techStackRepository.existsByName(request.name())) {
            throw new TrackException(TrackExceptionType.TECH_STACK_NAME_DUPLICATED);
        }

        TechStack techStack = techStackRepository.save(
                TechStack.builder().name(request.name()).iconUrl(request.iconUrl()).build());
        return TechStackResponse.from(techStack);
    }
}
