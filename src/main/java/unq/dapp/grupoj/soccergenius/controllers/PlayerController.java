package unq.dapp.grupoj.soccergenius.controllers;


import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unq.dapp.grupoj.soccergenius.model.dtos.PlayerPerformanceDto;
import unq.dapp.grupoj.soccergenius.services.player.PlayerService;

import java.util.logging.Logger;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService _playerService;
    private final Logger _logger = Logger.getLogger(PlayerController.class.getName());

    public PlayerController(PlayerService playerService) {
        this._playerService = playerService;
    }

    @GetMapping("/performance/{playerId}")
    public ResponseEntity<PlayerPerformanceDto> getPlayerPerformance(Long playerId) {
        //TODO
        PlayerPerformanceDto playerPerformance = _playerService.getPlayerPerformance(playerId);

        throw new NotImplementedException();
    }
}
