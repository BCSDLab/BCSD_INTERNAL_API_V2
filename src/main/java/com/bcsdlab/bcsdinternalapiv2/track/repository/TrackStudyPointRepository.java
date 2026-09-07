package com.bcsdlab.bcsdinternalapiv2.track.repository;

import com.bcsdlab.bcsdinternalapiv2.track.model.TrackStudyPoint;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackStudyPointRepository extends JpaRepository<TrackStudyPoint, Long> {

    List<TrackStudyPoint> findAllByTrackPage_IdOrderByDisplayOrderAsc(Long trackPageId);

    void deleteAllByTrackPage_Id(Long trackPageId);
}
