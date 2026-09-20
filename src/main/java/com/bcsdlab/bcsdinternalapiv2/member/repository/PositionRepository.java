package com.bcsdlab.bcsdinternalapiv2.member.repository;

import com.bcsdlab.bcsdinternalapiv2.member.model.Position;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findAllByCodeIn(List<String> codes);

    List<Position> findAllByActiveTrueOrderByIdAsc();
}
