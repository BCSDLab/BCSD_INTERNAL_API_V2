package com.bcsdlab.bcsdinternalapiv2.member.model;

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
 * 부원 직책 마스터. 한 부원은 여러 직책({@code member_position})을 동시에 가질 수 있다.
 */
@Getter
@Entity
@Table(name = "position")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Position extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, updatable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Builder
    private Position(String code, String name) {
        this.code = code;
        this.name = name;
        this.active = true;
    }
}
