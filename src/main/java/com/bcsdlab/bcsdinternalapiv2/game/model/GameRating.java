package com.bcsdlab.bcsdinternalapiv2.game.model;

import com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게임 등급 정보(FR-7.5). {@code game}과 1:1이며 선택 입력이다 — 행이 없으면 공개 응답의
 * {@code rating}이 {@code null}이다(AC-9.8). 내용정보 7종은 콤마 목록이 아니라 고정 컬럼으로
 * 둔다(INV-21) — 값 목록이 법정 고시 항목이라 DB 제약으로 미는 편이 검증 누락을 막는다.
 */
@Getter
@Entity
@Table(name = "game_rating")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameRating extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, updatable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", nullable = false)
    private GameRatingLevel rating;

    @Column(name = "classification_number")
    private String classificationNumber;

    @Column(name = "classification_date")
    private LocalDate classificationDate;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "developer_report_number")
    private String developerReportNumber;

    @Column(name = "desc_sexuality", nullable = false)
    private boolean descSexuality;

    @Column(name = "desc_violence", nullable = false)
    private boolean descViolence;

    @Column(name = "desc_fear", nullable = false)
    private boolean descFear;

    @Column(name = "desc_language", nullable = false)
    private boolean descLanguage;

    @Column(name = "desc_drugs", nullable = false)
    private boolean descDrugs;

    @Column(name = "desc_crime", nullable = false)
    private boolean descCrime;

    @Column(name = "desc_gambling", nullable = false)
    private boolean descGambling;

    @Builder
    private GameRating(Game game, GameRatingLevel rating, String classificationNumber,
                        LocalDate classificationDate, String businessName, String developerReportNumber,
                        boolean descSexuality, boolean descViolence, boolean descFear, boolean descLanguage,
                        boolean descDrugs, boolean descCrime, boolean descGambling) {
        this.game = game;
        this.rating = rating;
        this.classificationNumber = classificationNumber;
        this.classificationDate = classificationDate;
        this.businessName = businessName;
        this.developerReportNumber = developerReportNumber;
        this.descSexuality = descSexuality;
        this.descViolence = descViolence;
        this.descFear = descFear;
        this.descLanguage = descLanguage;
        this.descDrugs = descDrugs;
        this.descCrime = descCrime;
        this.descGambling = descGambling;
    }

    public void update(GameRatingLevel rating, String classificationNumber, LocalDate classificationDate,
                        String businessName, String developerReportNumber, Set<GameContentDescriptor> descriptors) {
        this.rating = rating;
        this.classificationNumber = classificationNumber;
        this.classificationDate = classificationDate;
        this.businessName = businessName;
        this.developerReportNumber = developerReportNumber;
        this.descSexuality = descriptors.contains(GameContentDescriptor.SEXUALITY);
        this.descViolence = descriptors.contains(GameContentDescriptor.VIOLENCE);
        this.descFear = descriptors.contains(GameContentDescriptor.FEAR);
        this.descLanguage = descriptors.contains(GameContentDescriptor.LANGUAGE);
        this.descDrugs = descriptors.contains(GameContentDescriptor.DRUGS);
        this.descCrime = descriptors.contains(GameContentDescriptor.CRIME);
        this.descGambling = descriptors.contains(GameContentDescriptor.GAMBLING);
    }
}
