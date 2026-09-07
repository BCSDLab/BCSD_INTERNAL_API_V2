package com.bcsdlab.bcsdinternalapiv2.reservation.repository;

import com.bcsdlab.bcsdinternalapiv2.reservation.model.ReservationGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationGroupRepository extends JpaRepository<ReservationGroup, Long> {
}
