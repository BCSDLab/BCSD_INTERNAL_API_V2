package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import java.util.List;
import org.springframework.data.domain.Page;

/** 관리자 인명부 응답. 페이지·집계 형식은 일반 인명부({@link MemberDirectoryResponse})와 같다. */
public record AdminMemberDirectoryResponse(
        List<AdminMemberSummaryResponse> members,
        MemberDirectoryResponse.PageInfo page,
        MemberDirectoryResponse.Counts counts
) {

    public static AdminMemberDirectoryResponse of(Page<Member> page, MemberDirectoryResponse.Counts counts) {
        List<AdminMemberSummaryResponse> members = page.getContent().stream()
                .map(AdminMemberSummaryResponse::from)
                .toList();
        MemberDirectoryResponse.PageInfo pageInfo = new MemberDirectoryResponse.PageInfo(page.getNumber(),
                page.getSize(), page.getTotalElements(), page.getTotalPages());
        return new AdminMemberDirectoryResponse(members, pageInfo, counts);
    }
}
