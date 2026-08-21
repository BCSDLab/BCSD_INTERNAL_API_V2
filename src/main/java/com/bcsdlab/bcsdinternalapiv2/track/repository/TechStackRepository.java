package com.bcsdlab.bcsdinternalapiv2.track.repository;

import com.bcsdlab.bcsdinternalapiv2.track.model.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {

    boolean existsByName(String name);
}
