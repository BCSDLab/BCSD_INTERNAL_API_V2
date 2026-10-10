package com.bcsdlab.bcsdinternalapiv2.ledger.repository;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemesterMember;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemesterMemberId;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DuesSemesterMemberRepository extends JpaRepository<DuesSemesterMember, DuesSemesterMemberId> {

    @Query("select r from DuesSemesterMember r join fetch r.member m join fetch m.track "
            + "where r.id.semesterId in :semesterIds")
    List<DuesSemesterMember> findAllWithMemberBySemesterIdIn(@Param("semesterIds") Collection<Long> semesterIds);

    /**
     * 납부 대상 여부를 바꿀 때 명단 행만 잠근다(회원·트랙 행까지 잠그지 않도록 fetch join을 쓰지 않는다).
     * 연결 생성(PR2)은 같은 행을 FOR SHARE로 잠가 비대상 전환과 엇갈리지 않게 한다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from DuesSemesterMember r where r.id.semesterId = :semesterId and r.id.memberId = :memberId")
    Optional<DuesSemesterMember> findForUpdate(@Param("semesterId") Long semesterId,
                                               @Param("memberId") Long memberId);

    /** 명단에 없는 모든 회원(회원 상태·회비 대상 여부와 무관). */
    @Query("select m from Member m join fetch m.track where not exists ("
            + "select 1 from DuesSemesterMember r where r.id.semesterId = :semesterId and r.id.memberId = m.id)")
    List<Member> findCandidates(@Param("semesterId") Long semesterId);

    /**
     * 학기 생성 시점의 명단 스냅샷. 동아리 활동 중이고 탈퇴하지 않은 회원을 넣고, 납부 대상 여부는 인명부의
     * dues_required를 따른다. PENDING_SETUP·LOCKED는 계정 상태일 뿐이라 포함한다.
     */
    @Modifying
    @Query(nativeQuery = true, value = """
            INSERT INTO dues_semester_member (semester_id, member_id, applicable)
            SELECT :semesterId, m.id, m.dues_required
            FROM member m
            WHERE m.is_active = TRUE AND m.status <> 'WITHDRAWN'
            """)
    int insertSnapshot(@Param("semesterId") Long semesterId);
}
