package com.bcsdlab.bcsdinternalapiv2.curriculum.repository;

import com.bcsdlab.bcsdinternalapiv2.curriculum.model.Curriculum;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {

    List<Curriculum> findAllByTrackPage_IdOrderByDisplayOrderAsc(Long trackPageId);

    Optional<Curriculum> findByTrackPage_IdAndPublishedTrue(Long trackPageId);

    /**
     * AC-2.1: 새 세트를 공개하기 전에, 같은 트랙의 다른 공개 세트를 먼저 비공개로 내린다.
     * 벌크 UPDATE로 즉시 실행되므로, 뒤이어 대상 엔티티를 공개로 바꿔도
     * uq_curriculum_published(partial unique index)에 걸리지 않는다.
     */
    @Modifying
    @Transactional
    @Query("update Curriculum c set c.published = false "
            + "where c.trackPage.id = :trackPageId and c.id <> :excludeId and c.published = true")
    int unpublishOthers(@Param("trackPageId") Long trackPageId, @Param("excludeId") Long excludeId);
}
