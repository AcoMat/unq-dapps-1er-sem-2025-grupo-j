package unq.dapp.grupoj.soccergenius.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unq.dapp.grupoj.soccergenius.model.dtos.ComparisonDto;
import unq.dapp.grupoj.soccergenius.model.dtos.MatchDTO;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamDto;
import unq.dapp.grupoj.soccergenius.services.team.TeamService;

import java.util.List;

@RestController
@RequestMapping("/teams")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
})
@Tag(name = "Team Management", description = "API for accessing team information, players, upcoming matches and comparisons")
public class TeamController {
    private static final Logger logger = LoggerFactory.getLogger(TeamController.class);
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/{teamName}/{country}/players")
    @Operation(
            summary = "Get team players",
            description = "Returns information on a team's players, including name, games played, goals, assists and rating."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved team players",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))
            ),
            @ApiResponse(responseCode = "404", description = "Team not found"),
            @ApiResponse(responseCode = "500", description = "Error during scraping process")
    })
    public ResponseEntity<List<String>> getTeamPlayers(
            @Parameter(description = "Name of the team", example = "Barcelona") @PathVariable String teamName,
            @Parameter(description = "Country of the team", example = "Spain") @PathVariable String country) {
        long startTime = System.currentTimeMillis();
        logger.info("Request received to get players from a team");
        List<String> players = this.teamService.getTeamPlayers(teamName, country);
        long endTime = System.currentTimeMillis();
        logger.info("Successfully retrieved {} players for team in {} ms", players.size(), (endTime - startTime));
        return ResponseEntity.status(HttpStatus.OK).body(players);
    }

    @GetMapping("/{teamName}/upcomingMatches")
    @Operation(
            summary = "Get upcoming matches",
            description = "Returns a list of upcoming games for the selected team"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved upcoming matches",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MatchDTO.class)))
            ),
            @ApiResponse(responseCode = "404", description = "Team not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<List<MatchDTO>> getUpcomingMatches(
            @Parameter(description = "Name of the team", example = "RealMadrid") @PathVariable String teamName) {
        List<MatchDTO> upcomingMatches = this.teamService.getUpcomingMatches(teamName);
        return ResponseEntity.status(HttpStatus.OK).body(upcomingMatches);
    }

    @GetMapping("/comparison")
    @Operation(
            summary = "Compare teams",
            description = "Allows to obtain comparative metrics between two teams"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teams compared successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid team names"),
    })
    public ResponseEntity<ComparisonDto> getTeamComparison(
            @Parameter(description = "Name of the first team to compare", example = "1") @RequestParam String teamAName,
            @Parameter(description = "Name of the second team to compare", example = "2") @RequestParam String teamBName) {

        ComparisonDto comparisonDto = this.teamService.getTeamsComparison(teamAName, teamBName);
        return ResponseEntity.status(HttpStatus.OK).body(comparisonDto);
    }

    @GetMapping("/{teamId}")
    @Operation(
            summary = "Get team by ID",
            description = "Returns detailed information about a specific team"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Team found",
                    content = @Content(schema = @Schema(implementation = TeamDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<TeamDto> getTeam(
            @Parameter(description = "ID of the team to retrieve", example = "BAR") @PathVariable Integer teamId) {
        logger.info("Request received to get all teams");
        TeamDto team = this.teamService.getTeamFromLaLigaById(teamId);
        return ResponseEntity.status(HttpStatus.OK).body(team);
    }
}
