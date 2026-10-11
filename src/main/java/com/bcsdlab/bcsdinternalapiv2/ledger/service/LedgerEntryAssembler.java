package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.DuesLinkResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.LedgerEntryResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesLink;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemester;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntry;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesLinkRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 장부 응답 조립. 내역 → 연결 → 회원·트랙·학기를 IN으로 한 번씩 읽어 N+1을 막는다.
 * 증빙은 PR3에서 붙인다(지금은 빈 목록).
 */
@Component
@RequiredArgsConstructor
public class LedgerEntryAssembler {

    private final DuesLinkRepository linkRepository;
    private final DuesSemesterRepository semesterRepository;

    public LedgerEntryResponse toResponse(LedgerEntry entry) {
        return toResponses(List.of(entry)).getFirst();
    }

    public List<LedgerEntryResponse> toResponses(List<LedgerEntry> entries) {
        if (entries.isEmpty()) {
            return List.of();
        }
        Map<Long, DuesLink> linksByEntry = linkRepository
                .findAllByEntryIdIn(entries.stream().map(LedgerEntry::getId).toList()).stream()
                .collect(Collectors.toMap(DuesLink::getEntryId, Function.identity()));
        Map<Long, Member> members = linksByEntry.isEmpty() ? Map.of() : linkRepository
                .findMembersWithTrack(linksByEntry.values().stream().map(DuesLink::getMemberId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));
        Map<Long, DuesSemester> semesters = linksByEntry.isEmpty() ? Map.of() : semesterRepository
                .findAllById(linksByEntry.values().stream().map(DuesLink::getSemesterId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(DuesSemester::getId, Function.identity()));
        return entries.stream().map(entry -> {
            DuesLink link = linksByEntry.get(entry.getId());
            DuesLinkResponse duesLink = link == null ? null : DuesLinkResponse.of(
                    link, members.get(link.getMemberId()), semesters.get(link.getSemesterId()).key());
            return LedgerEntryResponse.of(entry, duesLink, List.of());
        }).toList();
    }
}
