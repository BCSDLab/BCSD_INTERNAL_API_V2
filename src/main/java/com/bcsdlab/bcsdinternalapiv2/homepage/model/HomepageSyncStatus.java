package com.bcsdlab.bcsdinternalapiv2.homepage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 홈페이지 반영 웹훅의 상태를 담는 단일 행(id=1)이다(T-21, ADR-010). 여러 태그를
 * 콤마로 이어붙여 저장한다 — 이 정도 규모에 별도 자식 테이블은 과하다.
 */
@Getter
@Entity
@Table(name = "homepage_sync_status")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomepageSyncStatus {

    @Id
    private Long id;

    @Column(name = "last_succeeded_at")
    private Instant lastSucceededAt;

    @Column(name = "last_failed_at")
    private Instant lastFailedAt;

    @Column(name = "pending_tags")
    private String pendingTags;

    public List<String> getPendingTagList() {
        if (pendingTags == null || pendingTags.isBlank()) {
            return List.of();
        }
        return List.of(pendingTags.split(","));
    }

    public void markSucceeded(Instant at) {
        this.lastSucceededAt = at;
        this.pendingTags = null;
    }

    public void markFailed(Instant at, List<String> tags) {
        this.lastFailedAt = at;
        this.pendingTags = String.join(",", tags);
    }
}
