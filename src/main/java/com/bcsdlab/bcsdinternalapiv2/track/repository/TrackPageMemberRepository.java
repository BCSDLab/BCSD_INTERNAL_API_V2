package com.bcsdlab.bcsdinternalapiv2.track.repository;

import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPageMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackPageMemberRepository extends JpaRepository<TrackPageMember, Long> {

    List<TrackPageMember> findAllByTrackPage_IdOrderByDisplayOrderAsc(Long trackPageId);

    List<TrackPageMember> findAllByTrackPage_IdAndVisibleTrueOrderByDisplayOrderAsc(Long trackPageId);

    boolean existsByTrackPage_IdAndMember_Id(Long trackPageId, Long memberId);

    Optional<TrackPageMember> findByTrackPage_IdAndMember_Id(Long trackPageId, Long memberId);
}
