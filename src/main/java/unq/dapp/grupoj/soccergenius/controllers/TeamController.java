package unq.dapp.grupoj.soccergenius.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
import unq.dapp.grupoj.soccergenius.model.dtos.MatchDTO;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamDto;
import unq.dapp.grupoj.soccergenius.services.team.TeamService;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {
    private static final Logger logger = LoggerFactory.getLogger(TeamController.class);
    private final TeamService teamService;
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/{teamName}/{country}/players")
    @Operation(summary = "retorna información de los jugadores de un equipo, incluyendo nombre, partidos jugados, goles, asistencias y rating.")
    public ResponseEntity<List<String>> getTeamPlayers (@PathVariable String teamName, @PathVariable String country){
        String requestedTeamName = teamName.replaceAll("[\n\r]", "_");
        String requestedCountry = country.replaceAll("[\n\r]", "_");

        long startTime = System.currentTimeMillis();
        logger.info("Request received to get players from a team");

        try {
            List<String> players = this.teamService.getTeamPlayers(requestedTeamName, requestedCountry);
            long endTime = System.currentTimeMillis();
            logger.info("Successfully retrieved {} players for team in {} ms",
                    players.size(), (endTime - startTime));
            return ResponseEntity.status(HttpStatus.OK).body(players);
        } catch (Exception e) {
            throw new ScrappingException(e.getMessage());
        }
    }

    @GetMapping("/{teamName}/upcomingMatches")
    @Operation(summary = "retorna un listado de los proximos partidos para el equipo seleccionado")
    public ResponseEntity<List<MatchDTO>> getUpcomingMatches(@PathVariable String teamName){
        List<MatchDTO> upcomingMatches = this.teamService.getUpcomingMatches(teamName);
        return ResponseEntity.status(HttpStatus.OK).body(upcomingMatches);
    }

    @GetMapping("/comparison")
    @Operation(summary = "Permite obtener métricas comparativas entre dos equipos")
    public ResponseEntity<Object> getTeamComparison(@RequestParam String team1Id, @RequestParam String team2Id){
        return ResponseEntity.ok(null);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamDto> getTeam(@PathVariable int teamId) {
        logger.info("Request received to get all teams");
        TeamDto team = this.teamService.getTeamFromLaLigaById(teamId);
        return ResponseEntity.status(HttpStatus.OK).body(team);
    }
}
