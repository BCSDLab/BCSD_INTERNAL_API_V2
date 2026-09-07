package com.bcsdlab.bcsdinternalapiv2.homepage.repository;

import com.bcsdlab.bcsdinternalapiv2.homepage.model.HomepageSyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomepageSyncStatusRepository extends JpaRepository<HomepageSyncStatus, Long> {
}
