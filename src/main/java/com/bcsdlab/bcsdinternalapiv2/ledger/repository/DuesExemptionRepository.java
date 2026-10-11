package com.bcsdlab.bcsdinternalapiv2.ledger.repository;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesExemption;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DuesExemptionRepository extends JpaRepository<DuesExemption, Long> {

    List<DuesExemption> findAllByOrderByIdAsc();

    List<DuesExemption> findAllByMemberIdIn(Collection<Long> memberIds);
}
