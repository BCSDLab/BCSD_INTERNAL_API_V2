package com.bcsdlab.bcsdinternalapiv2.global;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;

/**
 * 최상위 엔티티(트랙 페이지·커리큘럼·활동 카테고리·활동 등)에만 적용하는 soft delete 규약.
 * 하위 트리(주차·토픽·세부항목·이미지)는 이 클래스를 쓰지 않고 {@code on delete cascade}로
 * 물리 삭제한다.
 *
 * <p><b>중요</b>: {@code @SQLRestriction("deleted_at is null")}은 이 매핑된 슈퍼클래스가 아니라
 * 이 클래스를 상속하는 각 {@code @Entity} 클래스에 직접 붙여야 한다. Hibernate의
 * {@code @SQLRestriction}은 매핑된 슈퍼클래스로부터 상속되지 않는다.
 */
@Getter
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseTimeEntity {

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void delete(Instant now) {
        this.deletedAt = now;
    }
}
