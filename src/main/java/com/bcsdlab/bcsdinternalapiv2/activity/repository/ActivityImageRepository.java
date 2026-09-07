package com.bcsdlab.bcsdinternalapiv2.activity.repository;

import com.bcsdlab.bcsdinternalapiv2.activity.model.ActivityImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityImageRepository extends JpaRepository<ActivityImage, Long> {

    List<ActivityImage> findAllByActivity_IdOrderByDisplayOrderAsc(Long activityId);

    List<ActivityImage> findAllByActivity_IdInOrderByDisplayOrderAsc(List<Long> activityIds);

    void deleteAllByActivity_Id(Long activityId);
}
