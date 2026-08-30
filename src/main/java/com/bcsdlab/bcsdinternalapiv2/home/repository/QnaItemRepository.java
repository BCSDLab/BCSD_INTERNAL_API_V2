package com.bcsdlab.bcsdinternalapiv2.home.repository;

import com.bcsdlab.bcsdinternalapiv2.home.model.QnaItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaItemRepository extends JpaRepository<QnaItem, Long> {

    List<QnaItem> findAllByOrderByDisplayOrderAsc();

    List<QnaItem> findAllByPublishedTrueOrderByDisplayOrderAsc();
}
