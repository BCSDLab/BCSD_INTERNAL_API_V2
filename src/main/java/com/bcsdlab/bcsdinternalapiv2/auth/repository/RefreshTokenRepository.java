package com.bcsdlab.bcsdinternalapiv2.auth.repository;

import com.bcsdlab.bcsdinternalapiv2.auth.model.RefreshToken;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByMemberIdAndRevokedAtIsNull(Long memberId);

    @Transactional
    @Modifying
    @Query("update RefreshToken r set r.revokedAt = :now where r.id = :id and r.revokedAt is null")
    int revokeIfNotRevoked(@Param("id") Long id, @Param("now") Instant now);
}
