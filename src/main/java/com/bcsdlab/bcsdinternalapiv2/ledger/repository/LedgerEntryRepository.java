package com.bcsdlab.bcsdinternalapiv2.ledger.repository;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntry;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    /** 화면 목록 순서. 은행 거래 순서 (occurred_at, occurred_seq, id)의 역순이다. */
    @Query("select e from LedgerEntry e order by e.occurredAt desc, e.occurredSeq desc, e.id desc")
    List<LedgerEntry> findAllLatestFirst();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from LedgerEntry e where e.id = :id")
    Optional<LedgerEntry> findByIdForUpdate(@Param("id") Long id);

    /** 일괄 연결용. 교착을 막으려고 id 오름차순으로 잠근다. 없는 id는 빠진다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from LedgerEntry e where e.id in :ids order by e.id")
    List<LedgerEntry> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);
}
