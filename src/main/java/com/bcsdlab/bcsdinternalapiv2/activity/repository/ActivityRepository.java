package com.bcsdlab.bcsdinternalapiv2.activity.repository;

import com.bcsdlab.bcsdinternalapiv2.activity.model.Activity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {

    boolean existsByCategory_Id(Long categoryId);

    List<Activity> findAllByCategory_IdAndYearAndMonthOrderByDisplayOrderAsc(Long categoryId, int year, int month);

    List<Activity> findAllByCategory_IdAndPublishedTrueOrderByYearDescMonthDescDisplayOrderAsc(Long categoryId);
}
