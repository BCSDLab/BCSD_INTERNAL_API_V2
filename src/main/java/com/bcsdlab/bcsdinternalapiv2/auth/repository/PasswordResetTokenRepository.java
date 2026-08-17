package com.bcsdlab.bcsdinternalapiv2.auth.repository;

import com.bcsdlab.bcsdinternalapiv2.auth.model.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update PasswordResetToken t set t.usedAt = :now where t.id = :id and t.usedAt is null")
    int markUsedIfUnused(@Param("id") Long id, @Param("now") Instant now);

    @Transactional
    @Modifying
    @Query("update PasswordResetToken t set t.usedAt = :now where t.memberId = :memberId and t.usedAt is null")
    int markAllUnusedAsUsed(@Param("memberId") Long memberId, @Param("now") Instant now);

    @Transactional
    @Modifying
    @Query("update PasswordResetToken t set t.mailSentAt = :now where t.id = :id")
    int markMailSent(@Param("id") Long id, @Param("now") Instant now);
}
