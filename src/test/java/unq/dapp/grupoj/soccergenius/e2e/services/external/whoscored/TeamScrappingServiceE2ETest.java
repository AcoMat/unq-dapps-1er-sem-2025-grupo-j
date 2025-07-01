package unq.dapp.grupoj.soccergenius.e2e.services.external.whoscored;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
import unq.dapp.grupoj.soccergenius.services.external.whoscored.TeamScrapingService;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("e2e")
@Tag("e2e")
@SpringBootTest
public class TeamScrappingServiceE2ETest {

    private final TeamScrapingService service = new TeamScrapingService();

    /* SELENIUM FALLA EN GITHUB ACTIONS
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
        int teamId = 52; // Real madrid

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

     */

    @Test
    void scrapTeamStatisticsById_invalidTeamId_shouldThrowException() {
        int invalidTeamId = 99999; // Assuming this ID does not exist

        assertThrows(ScrappingException.class, () -> service.scrapTeamStatisticsById(invalidTeamId));
    }


}
