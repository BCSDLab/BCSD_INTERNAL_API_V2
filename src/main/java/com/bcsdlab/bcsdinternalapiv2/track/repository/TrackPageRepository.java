package com.bcsdlab.bcsdinternalapiv2.track.repository;

import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackPageRepository extends JpaRepository<TrackPage, Long> {

    List<TrackPage> findAllByPublishedTrueOrderByDisplayOrderAsc();

    Optional<TrackPage> findBySlugAndPublishedTrue(String slug);
}
