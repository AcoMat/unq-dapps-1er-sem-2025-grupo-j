package unq.dapp.grupoj.soccergenius.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unq.dapp.grupoj.soccergenius.model.player.Player;
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
    public ResponseEntity<String> getPlayerPerformance(@PathVariable int playerId) {
        String performance = _playerService.getPlayerPerformance(playerId);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<Object> getPlayer(@PathVariable int playerId) {
        Player player = _playerService.getPlayer(playerId);
        if(player == null) {
            _logger.warning("Player not found with id: " + playerId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(player);
    }
}
