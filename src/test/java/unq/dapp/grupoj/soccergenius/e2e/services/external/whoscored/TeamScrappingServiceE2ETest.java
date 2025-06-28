package unq.dapp.grupoj.soccergenius.e2e.services.external.whoscored;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamStatisticsDTO;
import unq.dapp.grupoj.soccergenius.services.external.whoscored.TeamScrapingService;
import unq.dapp.grupoj.soccergenius.util.InputSanitizer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("e2e")
@Tag("e2e")
@SpringBootTest
public class TeamScrappingServiceE2ETest {

    private final TeamScrapingService service = new TeamScrapingService();

    @Test
    void getPlayersNamesFromTeam_validTeamName_shouldReturnPlayersList() {
        String teamName = InputSanitizer.sanitizeInput("Real Madrid");
        String country = InputSanitizer.sanitizeInput("España");

        var players = service.getPlayersNamesFromTeam(teamName, country);

        assertNotNull(players);
        assertFalse(players.isEmpty());
    }

    @Test
    void scrapTeamStatisticsById_validTeamId_shouldReturnValidStats() {
        int teamId = 86; // Real madrid

        TeamStatisticsDTO result = service.scrapTeamStatisticsById(teamId);

        assertNotNull(result);
        assertFalse(result.getName().isEmpty());
        assertFalse(result.getTotalMatchesPlayedStr().isEmpty());
        assertFalse(result.getTotalGoalsStr().isEmpty());
        assertFalse(result.getAvgShotsPerGameStr().isEmpty());
        assertFalse(result.getAvgPossessionStr().isEmpty());
        assertFalse(result.getAvgPassSuccessStr().isEmpty());
        assertFalse(result.getAvgAerialWonPerGameStr().isEmpty());
        assertFalse(result.getOverallRatingStr().isEmpty());
        assertFalse(result.getTotalYellowCardsStr().isEmpty());
        assertFalse(result.getTotalRedCardsStr().isEmpty());
    }

    @Test
    void scrapTeamStatisticsById_invalidTeamId_shouldReturnNull() {
        int invalidTeamId = 9999; // Assuming this ID does not exist

        TeamStatisticsDTO result = service.scrapTeamStatisticsById(invalidTeamId);

        assertNotNull(result);
        assertFalse(result.getName().isEmpty());
        assertFalse(result.getTotalMatchesPlayedStr().isEmpty());
        assertFalse(result.getTotalGoalsStr().isEmpty());
        assertFalse(result.getAvgShotsPerGameStr().isEmpty());
        assertFalse(result.getAvgPossessionStr().isEmpty());
        assertFalse(result.getAvgPassSuccessStr().isEmpty());
        assertFalse(result.getAvgAerialWonPerGameStr().isEmpty());
        assertFalse(result.getOverallRatingStr().isEmpty());
        assertFalse(result.getTotalYellowCardsStr().isEmpty());
        assertFalse(result.getTotalRedCardsStr().isEmpty());
    }


}
