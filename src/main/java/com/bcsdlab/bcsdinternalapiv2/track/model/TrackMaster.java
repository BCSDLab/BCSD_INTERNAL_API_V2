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
 * 인터널 공용 트랙 마스터. 부원의 소속(member.track_id)이 참조한다.
 *
 * <p>홈페이지 노출 프로필({@code track_page}, T-05/T-06)과 의도적으로 분리되어 있다 — 이 클래스는
 * "부원이 어디에 속하는가"만 다룬다. 클래스명을 {@code Track}이 아니라 {@code TrackMaster}로 둔 이유는
 * {@link com.bcsdlab.bcsdinternalapiv2.member.model.Track}(API 요청/응답에 쓰는 코드 enum)과
 * 이름이 겹치면 같은 파일에서 두 타입을 함께 참조해야 하는 곳(예: 요청 DTO → 엔티티 변환)에서
 * import 충돌이 나기 때문이다.
 */
@Getter
@Entity
@Table(name = "track")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrackMaster extends BaseTimeEntity {

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
    private TrackMaster(String code, String name) {
        this.code = code;
        this.name = name;
        this.active = true;
    }

    public void update(String name, boolean active) {
        this.name = name;
        this.active = active;
    }
}
