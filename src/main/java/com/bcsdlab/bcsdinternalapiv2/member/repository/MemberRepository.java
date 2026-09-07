package com.bcsdlab.bcsdinternalapiv2.member.repository;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {

    Optional<Member> findByStudentNumber(String studentNumber);

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByStudentNumber(String studentNumber);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id = :id")
    Optional<Member> findByIdForUpdate(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("update Member m set m.welcomeMailSentAt = :now "
            + "where m.id = :id and m.passwordChangedAt = :expectedPasswordChangedAt")
    int markWelcomeMailSentIfCurrent(@Param("id") Long id, @Param("now") Instant now,
                                      @Param("expectedPasswordChangedAt") Instant expectedPasswordChangedAt);

    @Transactional
    @Modifying
    @Query("update Member m set m.role = :role where m.id = :id")
    int updateRole(@Param("id") Long id, @Param("role") MemberRole role);

    long countByClubActive(boolean clubActive);

    @Query("select m.academicStatus, count(m) from Member m group by m.academicStatus")
    List<Object[]> countGroupByAcademicStatus();

    @Query("select m.track.code, count(m) from Member m group by m.track.code")
    List<Object[]> countGroupByTrack();

    @Query("select m.memberType, count(m) from Member m group by m.memberType")
    List<Object[]> countGroupByMemberType();
}
