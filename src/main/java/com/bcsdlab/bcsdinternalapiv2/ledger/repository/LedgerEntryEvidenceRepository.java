package com.bcsdlab.bcsdinternalapiv2.ledger.repository;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntryEvidence;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryEvidenceRepository extends JpaRepository<LedgerEntryEvidence, Long> {

    List<LedgerEntryEvidence> findAllByEntryId(Long entryId);

    /** 응답 조립용. 증빙 순서는 image_asset id 오름차순이다. */
    @Query("select new com.bcsdlab.bcsdinternalapiv2.ledger.repository.EntryEvidenceRow(e.entryId, a) "
            + "from LedgerEntryEvidence e join ImageAsset a on a.id = e.imageAssetId "
            + "where e.entryId in :entryIds order by a.id")
    List<EntryEvidenceRow> findEvidencesByEntryIdIn(@Param("entryIds") Collection<Long> entryIds);
}
