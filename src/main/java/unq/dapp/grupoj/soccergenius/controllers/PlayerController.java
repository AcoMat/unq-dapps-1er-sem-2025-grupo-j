package unq.dapp.grupoj.soccergenius.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.services.player.PlayerService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.logging.Logger;

@RestController
@RequestMapping("/players")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
})
@Tag(name = "Player", description = "Player management APIs")
public class PlayerController {

    private final PlayerService playerService;
    private final Logger logger = Logger.getLogger(PlayerController.class.getName());

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/performance/{playerId}")
    @Operation(summary = "Returns the performance index and tendency of a player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved player performance"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    public ResponseEntity<String> getPlayerPerformance(@PathVariable int playerId) {
        String performance = playerService.getPlayerPerformance(playerId);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/{playerId}")
    @Operation(summary = "Returns a player by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved player"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    public ResponseEntity<Object> getPlayer(@PathVariable int playerId) {
        Player player = playerService.getPlayer(playerId);
        if(player == null) {
            logger.log(java.util.logging.Level.WARNING, "Player not found with id: {0}", playerId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(player);
    }
}
