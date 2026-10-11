package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.EvidenceLinkRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntryEvidenceLink;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.LedgerEntryEvidenceLinkRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 증빙 링크 쓰기. 링크는 외부 호출 없이 DB에만 남는다. 호출하는 쪽이 내역 행을 잠갔거나 방금 만든 상태에서 부른다. */
@Component
@RequiredArgsConstructor
public class EvidenceLinkWriter {

    private final LedgerEntryEvidenceLinkRepository linkRepository;
    private final Clock clock;

    /**
     * 같은 기록 안에 같은 url이 두 번 오면 400, 새로 붙이는 링크가 구글 드라이브 https 링크가 아니면 400.
     * 이미 그 기록에 붙어 있던 url(alreadyAttached)은 드라이브 검사를 하지 않는다 — V27이 옮긴 예전 S3 증빙을
     * 남긴 채 다른 항목을 고칠 수 있게. 개수(최대 5)·빈 값·길이는 요청 검증이 먼저 막는다.
     */
    public static void requireAttachable(List<EvidenceLinkRequest> links, Collection<String> alreadyAttached) {
        if (EvidenceLinkRequest.hasDuplicateUrl(links)) {
            throw new LedgerException(LedgerExceptionType.DUPLICATED_EVIDENCE_LINK_IN_REQUEST);
        }
        for (EvidenceLinkRequest link : links) {
            if (!alreadyAttached.contains(link.url()) && !link.isGoogleDriveUrl()) {
                throw new LedgerException(LedgerExceptionType.EVIDENCE_LINK_NOT_GOOGLE_DRIVE);
            }
        }
    }

    /**
     * 링크 목록을 통째로 바꾼다. 보낸 목록에 있는 url은 행을 두고 이름만 고치고, 빠진 url은 지우고, 새 url은 붙인다.
     * 응답 순서는 붙인 순서(id)라 남은 링크가 앞에 온다.
     */
    @Transactional
    public void replace(Long entryId, List<EvidenceLinkRequest> links) {
        List<LedgerEntryEvidenceLink> existing = linkRepository.findAllByEntryId(entryId);
        List<String> existingUrls = existing.stream().map(LedgerEntryEvidenceLink::getUrl).toList();
        requireAttachable(links, existingUrls);
        Map<String, EvidenceLinkRequest> requested = links.stream()
                .collect(Collectors.toMap(EvidenceLinkRequest::url, Function.identity()));
        for (LedgerEntryEvidenceLink link : existing) {
            EvidenceLinkRequest kept = requested.get(link.getUrl());
            if (kept == null) {
                linkRepository.delete(link);
            } else if (!Objects.equals(link.getLabel(), kept.label())) {
                link.relabel(kept.label());
            }
        }
        add(entryId, links.stream().filter(link -> !existingUrls.contains(link.url())).toList());
    }

    /** 검사를 마친 새 링크를 보낸 순서대로 붙인다(가져오기는 호출하는 쪽이 requireAttachable을 먼저 부른다). */
    @Transactional
    public void add(Long entryId, List<EvidenceLinkRequest> links) {
        Instant now = Instant.now(clock);
        linkRepository.saveAll(links.stream()
                .map(link -> new LedgerEntryEvidenceLink(entryId, link.url(), link.label(), now))
                .toList());
    }
}
