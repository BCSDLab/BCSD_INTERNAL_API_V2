package com.bcsdlab.bcsdinternalapiv2.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "remember_me", nullable = false)
    private boolean rememberMe;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "rotated_from_id")
    private Long rotatedFromId;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Builder
    private RefreshToken(Long memberId, String tokenHash, Instant expiresAt, boolean rememberMe,
                          Long rotatedFromId, String userAgent, Instant createdAt) {
        this.memberId = memberId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.rememberMe = rememberMe;
        this.rotatedFromId = rotatedFromId;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(this.expiresAt);
    }

    public boolean isRevoked() {
        return this.revokedAt != null;
    }

    public void revoke(Instant now) {
        this.revokedAt = now;
    }
}
