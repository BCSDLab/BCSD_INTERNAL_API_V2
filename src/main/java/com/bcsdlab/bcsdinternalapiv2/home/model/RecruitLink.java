package com.bcsdlab.bcsdinternalapiv2.home.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 모집 링크 설정(FR-8.3). 테이블에 행이 항상 정확히 1개다(INV-22) — 시퀀스로 채번하지
 * 않고 고정 id(1)를 쓴다. {@code BaseTimeEntity}를 쓰지 않는 이유는 이 테이블에
 * {@code created_at}이 없기 때문이다(첫 upsert 시점이 곧 유일한 생성 시점이라
 * 별 의미가 없다) — {@code updated_at}만 직접 관리한다.
 */
@Getter
@Entity
@Table(name = "recruit_link")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitLink {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "google_form_url", nullable = false)
    private String googleFormUrl;

    @Column(name = "is_open", nullable = false)
    private boolean open;

    @Column(name = "close_date")
    private LocalDate closeDate;

    @Column(name = "closed_message", nullable = false)
    private String closedMessage;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static RecruitLink create(String googleFormUrl, boolean open, LocalDate closeDate,
                                      String closedMessage, Instant now) {
        RecruitLink link = new RecruitLink();
        link.id = SINGLETON_ID;
        link.update(googleFormUrl, open, closeDate, closedMessage, now);
        return link;
    }

    public void update(String googleFormUrl, boolean open, LocalDate closeDate, String closedMessage, Instant now) {
        this.googleFormUrl = googleFormUrl;
        this.open = open;
        this.closeDate = closeDate;
        this.closedMessage = closedMessage;
        this.updatedAt = now;
    }
}
