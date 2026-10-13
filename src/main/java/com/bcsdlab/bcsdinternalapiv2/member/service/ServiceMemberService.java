package com.bcsdlab.bcsdinternalapiv2.member.service;

import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.ServiceMemberListResponse;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.ServiceMemberResponse;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 내부 서비스(bbot)용 읽기 전용 명부. 탈퇴한 회원만 제외하고 id 순으로 내려준다. */
@Service
@RequiredArgsConstructor
public class ServiceMemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public ServiceMemberListResponse getMembers() {
        return new ServiceMemberListResponse(
                memberRepository.findAllWithTrackByStatusNotOrderById(MemberStatus.WITHDRAWN).stream()
                        .map(ServiceMemberResponse::from)
                        .toList());
    }
}
