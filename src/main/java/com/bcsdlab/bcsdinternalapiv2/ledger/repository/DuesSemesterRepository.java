package com.bcsdlab.bcsdinternalapiv2.ledger.repository;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemester;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DuesSemesterRepository extends JpaRepository<DuesSemester, Long> {

    Optional<DuesSemester> findByYearAndTerm(short year, short term);

    boolean existsByYearAndTerm(short year, short term);

    @Query("select s from DuesSemester s order by s.year desc, s.term desc")
    List<DuesSemester> findAllLatestFirst();
}
