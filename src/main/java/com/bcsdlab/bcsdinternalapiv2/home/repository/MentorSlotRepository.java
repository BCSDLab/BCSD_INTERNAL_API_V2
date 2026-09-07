package com.bcsdlab.bcsdinternalapiv2.home.repository;

import com.bcsdlab.bcsdinternalapiv2.home.model.MentorSlot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorSlotRepository extends JpaRepository<MentorSlot, Long> {

    List<MentorSlot> findAllByOrderByDisplayOrderAsc();

    boolean existsByMember_Id(Long memberId);

    Optional<MentorSlot> findByMember_Id(Long memberId);
}
