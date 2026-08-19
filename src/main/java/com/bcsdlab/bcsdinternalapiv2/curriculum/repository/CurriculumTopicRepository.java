package com.bcsdlab.bcsdinternalapiv2.curriculum.repository;

import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumTopic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurriculumTopicRepository extends JpaRepository<CurriculumTopic, Long> {

    List<CurriculumTopic> findAllByWeek_IdOrderByDisplayOrderAsc(Long weekId);
}
