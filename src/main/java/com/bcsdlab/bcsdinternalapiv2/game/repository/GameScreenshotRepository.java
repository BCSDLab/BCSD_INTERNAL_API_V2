package com.bcsdlab.bcsdinternalapiv2.game.repository;

import com.bcsdlab.bcsdinternalapiv2.game.model.GameScreenshot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameScreenshotRepository extends JpaRepository<GameScreenshot, Long> {

    List<GameScreenshot> findAllByGame_IdOrderByDisplayOrderAsc(Long gameId);
}
