package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

public record MemberDirectoryResponse(
        List<MemberSummaryResponse> members,
        PageInfo page,
        Counts counts
) {

    public record PageInfo(int number, int size, long totalElements, int totalPages) {
    }

    public record Counts(
            long total,
            long active,
            long inactive,
            Map<String, Long> byAcademicStatus,
            Map<String, Long> byTrack,
            Map<String, Long> byMemberType
    ) {
    }

    public static MemberDirectoryResponse of(Page<Member> page, Counts counts) {
        List<MemberSummaryResponse> members = page.getContent().stream()
                .map(MemberSummaryResponse::from)
                .toList();
        PageInfo pageInfo = new PageInfo(page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages());
        return new MemberDirectoryResponse(members, pageInfo, counts);
    }
}
