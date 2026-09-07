package com.bcsdlab.bcsdinternalapiv2.game.controller;

import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.GameDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.GameSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.game.service.GameService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/games")
@RequiredArgsConstructor
public class GameController implements GameApi {

    private final GameService gameService;

    @Override
    @GetMapping
    public List<GameSummaryResponse> getGames() {
        return gameService.getGames();
    }

    @Override
    @GetMapping("/{slug}")
    public GameDetailResponse getGame(@PathVariable String slug) {
        return gameService.getGame(slug);
    }
}
