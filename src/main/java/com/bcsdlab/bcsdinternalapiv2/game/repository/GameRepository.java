package com.bcsdlab.bcsdinternalapiv2.game.repository;

import com.bcsdlab.bcsdinternalapiv2.game.model.Game;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findAllByPublishedTrueOrderByDisplayOrderAsc();

    List<Game> findAllByOrderByDisplayOrderAsc();

    Optional<Game> findBySlugAndPublishedTrue(String slug);

    boolean existsBySlug(String slug);
}
