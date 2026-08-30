package com.bcsdlab.bcsdinternalapiv2.game.repository;

import com.bcsdlab.bcsdinternalapiv2.game.model.GameRating;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRatingRepository extends JpaRepository<GameRating, Long> {

    Optional<GameRating> findByGame_Id(Long gameId);
}
