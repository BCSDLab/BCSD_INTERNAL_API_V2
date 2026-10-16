package com.bcsdlab.bcsdinternalapiv2.home.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.security.LoginMemberId;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request.RecruitLinkUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.AdminRecruitLinkResponse;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.RecruitLinkHistoryResponse;
import com.bcsdlab.bcsdinternalapiv2.home.service.AdminRecruitLinkService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/recruit-link")
@RequiredArgsConstructor
public class AdminRecruitLinkController implements AdminRecruitLinkApi {

    private final AdminRecruitLinkService adminRecruitLinkService;

    @Override
    @GetMapping
    public AdminRecruitLinkResponse getCurrent() {
        return adminRecruitLinkService.getCurrent();
    }

    @Override
    @PutMapping
    public AdminRecruitLinkResponse update(@Valid @RequestBody RecruitLinkUpdateRequest request,
                                            @LoginMemberId Long loginMemberId) {
        return adminRecruitLinkService.update(request, loginMemberId);
    }

    @Override
    @GetMapping("/history")
    public List<RecruitLinkHistoryResponse> getHistory() {
        return adminRecruitLinkService.getHistory();
    }
}
