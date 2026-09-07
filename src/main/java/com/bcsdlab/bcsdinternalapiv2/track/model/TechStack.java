package com.bcsdlab.bcsdinternalapiv2.track.model;

import com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기술스택 마스터. 여러 트랙 페이지가 공유한다(React, Java 등 재사용).
 */
@Getter
@Entity
@Table(name = "tech_stack")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechStack extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "icon_url", nullable = false)
    private String iconUrl;

    @Builder
    private TechStack(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
    }
}
