package com.bcsdlab.bcsdinternalapiv2.curriculum.repository;

import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumTopicDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurriculumTopicDetailRepository extends JpaRepository<CurriculumTopicDetail, Long> {

    List<CurriculumTopicDetail> findAllByTopic_IdOrderByDisplayOrderAsc(Long topicId);

    void deleteAllByTopic_Id(Long topicId);
}
