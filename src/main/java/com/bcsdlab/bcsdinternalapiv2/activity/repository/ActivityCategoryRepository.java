package com.bcsdlab.bcsdinternalapiv2.activity.repository;

import com.bcsdlab.bcsdinternalapiv2.activity.model.ActivityCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityCategoryRepository extends JpaRepository<ActivityCategory, Long> {

    List<ActivityCategory> findAllByOrderByDisplayOrderAsc();

    List<ActivityCategory> findAllByPublishedTrueOrderByDisplayOrderAsc();

    Optional<ActivityCategory> findBySlugAndPublishedTrue(String slug);

    boolean existsBySlug(String slug);
}
