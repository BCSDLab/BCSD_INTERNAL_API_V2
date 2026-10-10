package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.DuesLinkBulkRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.DuesLinkRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.DuesLinkBulkResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.LedgerEntryResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesLink;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemester;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemesterMember;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntry;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesLinkRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.LedgerEntryRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 장부 기록 ↔ 회비 연결. 연결하면 분류는 입금·출금 모두 DUES가 되고, 금액 검증은 하지 않는다.
 *
 * <p>잠금 순서는 항상 명단 행(FOR SHARE) → 내역 행(FOR UPDATE)이다. 명단 행을 FOR SHARE로 잠그면 그 회원이
 * 연결하는 동안 비대상으로 바뀌지 않는다(비대상 전환은 같은 행을 FOR UPDATE로 잠근다). 그래서 "비대상 회원에게는
 * 연결이 없다"가 깨지지 않는다.
 */
@Service
@RequiredArgsConstructor
public class DuesLinkService {

    private final Clock clock;
    private final DuesSemesterReader semesterReader;
    private final DuesSemesterMemberRepository rosterRepository;
    private final LedgerEntryRepository entryRepository;
    private final DuesLinkRepository linkRepository;
    private final DuesLinkWriter linkWriter;
    private final LedgerEntryAssembler assembler;

    /**
     * 단건 연결. 이미 연결돼 있으면 새 대상으로 옮긴다. 옮길 때 기존 회원의 명단 행은 잠그지 않는다 — 명단 행은
     * 지워지지 않고, 기존 회원이 동시에 비대상으로 바뀌어도 최악은 그 요청이 409를 받는 것뿐이다.
     */
    @Transactional
    public LedgerEntryResponse link(Long entryId, DuesLinkRequest request, Long adminId) {
        DuesSemester semester = semesterReader.get(request.semesterId());
        lockLinkableMember(semester.getId(), request.memberId());
        LedgerEntry entry = entryRepository.findByIdForUpdate(entryId)
                .orElseThrow(() -> new LedgerException(LedgerExceptionType.ENTRY_NOT_FOUND));

        Instant now = Instant.now(clock);
        Optional<DuesLink> existing = linkRepository.findById(entryId);
        if (existing.isPresent()) {
            existing.get().moveTo(semester.getId(), request.memberId(), adminId, now);
        } else {
            linkWriter.insert(entryId, semester.getId(), request.memberId(), adminId, now);
        }
        entry.markAsDues();
        return assembler.toResponse(entry);
    }

    /** 연결 해제. 분류는 그대로라 결과는 PENDING(DUES) 또는 NONE이다. 연결이 없어도 성공한다. */
    @Transactional
    public void unlink(Long entryId) {
        entryRepository.findByIdForUpdate(entryId)
                .orElseThrow(() -> new LedgerException(LedgerExceptionType.ENTRY_NOT_FOUND));
        linkRepository.findById(entryId).ifPresent(linkRepository::delete);
    }

    /**
     * 일괄 연결. 회원을 모두 먼저 확인하고, 명단에 없거나(404) 비대상인(409) 회원이 하나라도 있으면 아무것도
     * 연결하지 않는다(요청 순서상 처음 실패한 회원이 오류를 정한다). 없는 내역과 이미 연결된 내역은 건너뛰고 세지
     * 않는다.
     */
    @Transactional
    public DuesLinkBulkResponse linkAll(String semesterId, DuesLinkBulkRequest request, Long adminId) {
        List<DuesLinkBulkRequest.Link> links = request.links();
        List<Long> entryIds = links.stream().map(DuesLinkBulkRequest.Link::entryId).toList();
        if (new HashSet<>(entryIds).size() != entryIds.size()) {
            throw new LedgerException(LedgerExceptionType.DUPLICATED_ENTRY_IN_REQUEST);
        }
        DuesSemester semester = semesterReader.get(semesterId);

        List<Long> memberIds = links.stream().map(DuesLinkBulkRequest.Link::memberId).distinct().sorted().toList();
        Map<Long, DuesSemesterMember> rows = rosterRepository.findAllForShare(semester.getId(), memberIds).stream()
                .collect(Collectors.toMap(row -> row.getId().getMemberId(), Function.identity()));
        for (DuesLinkBulkRequest.Link link : links) {
            requireLinkable(rows.get(link.memberId()));
        }

        Map<Long, LedgerEntry> entries = entryRepository.findAllByIdInForUpdate(entryIds).stream()
                .collect(Collectors.toMap(LedgerEntry::getId, Function.identity()));
        Set<Long> linked = linkRepository.findAllByEntryIdIn(entries.keySet()).stream()
                .map(DuesLink::getEntryId)
                .collect(Collectors.toSet());
        Instant now = Instant.now(clock);
        int linkedCount = 0;
        for (DuesLinkBulkRequest.Link link : links) {
            LedgerEntry entry = entries.get(link.entryId());
            if (entry == null || linked.contains(entry.getId())) {
                continue;
            }
            linkWriter.insert(entry.getId(), semester.getId(), link.memberId(), adminId, now);
            entry.markAsDues();
            linkedCount++;
        }
        return new DuesLinkBulkResponse(linkedCount);
    }

    private void lockLinkableMember(Long semesterId, Long memberId) {
        requireLinkable(rosterRepository.findForShare(semesterId, memberId).orElse(null));
    }

    private void requireLinkable(DuesSemesterMember row) {
        if (row == null) {
            throw new LedgerException(LedgerExceptionType.NOT_ROSTER_MEMBER);
        }
        if (!row.isApplicable()) {
            throw new LedgerException(LedgerExceptionType.ROSTER_MEMBER_NOT_APPLICABLE);
        }
    }
}
