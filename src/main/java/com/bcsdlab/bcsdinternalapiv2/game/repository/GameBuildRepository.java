package com.bcsdlab.bcsdinternalapiv2.game.repository;

import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuild;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuildStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameBuildRepository extends JpaRepository<GameBuild, Long> {

    Optional<GameBuild> findFirstByGame_IdAndStatusOrderByUploadedAtDesc(Long gameId, GameBuildStatus status);

    List<GameBuild> findAllByGame_IdOrderByUploadedAtDesc(Long gameId);

    List<GameBuild> findAllByGame_IdAndStatus(Long gameId, GameBuildStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from GameBuild b where b.id = :id")
    Optional<GameBuild> findByIdForUpdate(@Param("id") Long id);
}
