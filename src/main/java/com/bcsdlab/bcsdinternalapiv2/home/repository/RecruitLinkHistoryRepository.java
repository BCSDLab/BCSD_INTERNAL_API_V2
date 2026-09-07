package com.bcsdlab.bcsdinternalapiv2.home.repository;

import com.bcsdlab.bcsdinternalapiv2.home.model.RecruitLinkHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitLinkHistoryRepository extends JpaRepository<RecruitLinkHistory, Long> {

    List<RecruitLinkHistory> findAllByOrderByChangedAtDesc();
}
