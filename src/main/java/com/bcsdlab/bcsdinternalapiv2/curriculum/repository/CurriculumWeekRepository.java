package com.bcsdlab.bcsdinternalapiv2.curriculum.repository;

import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurriculumWeekRepository extends JpaRepository<CurriculumWeek, Long> {

    List<CurriculumWeek> findAllByCurriculum_IdOrderByDisplayOrderAsc(Long curriculumId);
}
