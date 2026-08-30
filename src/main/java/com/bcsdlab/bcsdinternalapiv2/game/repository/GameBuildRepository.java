package com.bcsdlab.bcsdinternalapiv2.game.repository;

import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuild;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuildStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameBuildRepository extends JpaRepository<GameBuild, Long> {

    Optional<GameBuild> findFirstByGame_IdAndStatusOrderByUploadedAtDesc(Long gameId, GameBuildStatus status);

    List<GameBuild> findAllByGame_IdOrderByUploadedAtDesc(Long gameId);
}
