package com.bcsdlab.bcsdinternalapiv2.ledger.repository;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesLink;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DuesLinkRepository extends JpaRepository<DuesLink, Long> {

    boolean existsBySemesterIdAndMemberId(Long semesterId, Long memberId);

    List<DuesLink> findAllByEntryIdIn(Collection<Long> entryIds);

    @Query("select new com.bcsdlab.bcsdinternalapiv2.ledger.repository.LinkedEntryRow(l.semesterId, l.memberId, e.type, e.amount) "
            + "from DuesLink l join LedgerEntry e on e.id = l.entryId where l.semesterId in :semesterIds")
    List<LinkedEntryRow> findLinkedEntriesBySemesterIdIn(@Param("semesterIds") Collection<Long> semesterIds);

    @Query("select m from Member m join fetch m.track where m.id in :ids")
    List<Member> findMembersWithTrack(@Param("ids") Collection<Long> ids);
}
