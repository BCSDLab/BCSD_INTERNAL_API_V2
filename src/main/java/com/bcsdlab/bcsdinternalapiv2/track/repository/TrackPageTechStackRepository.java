package com.bcsdlab.bcsdinternalapiv2.track.repository;

import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPageTechStack;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPageTechStackId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrackPageTechStackRepository extends JpaRepository<TrackPageTechStack, TrackPageTechStackId> {

    @Query("select tpts from TrackPageTechStack tpts join fetch tpts.techStack "
            + "where tpts.trackPage.id = :trackPageId order by tpts.displayOrder asc")
    List<TrackPageTechStack> findAllByTrackPageIdOrderByDisplayOrderAsc(@Param("trackPageId") Long trackPageId);

    void deleteAllByTrackPage_Id(Long trackPageId);
}
