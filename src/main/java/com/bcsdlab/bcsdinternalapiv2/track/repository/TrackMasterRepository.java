package com.bcsdlab.bcsdinternalapiv2.track.repository;

import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackMasterRepository extends JpaRepository<TrackMaster, Long> {

    Optional<TrackMaster> findByCode(String code);

    boolean existsByCode(String code);
}
