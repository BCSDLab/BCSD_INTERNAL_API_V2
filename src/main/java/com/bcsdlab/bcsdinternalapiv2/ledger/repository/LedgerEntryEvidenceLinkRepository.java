package com.bcsdlab.bcsdinternalapiv2.ledger.repository;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntryEvidenceLink;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryEvidenceLinkRepository extends JpaRepository<LedgerEntryEvidenceLink, Long> {

    List<LedgerEntryEvidenceLink> findAllByEntryId(Long entryId);

    /** 응답 조립용. 링크 순서는 id 오름차순(붙인 순서)이다. */
    List<LedgerEntryEvidenceLink> findAllByEntryIdInOrderByIdAsc(Collection<Long> entryIds);
}
