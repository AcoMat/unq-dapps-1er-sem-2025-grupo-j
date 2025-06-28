package unq.dapp.grupoj.soccergenius.unit.services;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
import unq.dapp.grupoj.soccergenius.mappers.Mapper;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.model.dtos.*;
import unq.dapp.grupoj.soccergenius.model.dtos.external.football_data.FootballDataMatchDto;
import unq.dapp.grupoj.soccergenius.model.dtos.external.football_data.FootballDataMatchsDto;
import unq.dapp.grupoj.soccergenius.model.dtos.external.football_data.FootballDataTeamDto;
import unq.dapp.grupoj.soccergenius.repository.TeamRepository;
import unq.dapp.grupoj.soccergenius.services.external.football_data.FootballDataApiService;
import unq.dapp.grupoj.soccergenius.services.external.whoscored.TeamScrapingService;
import unq.dapp.grupoj.soccergenius.services.team.TeamServiceImpl;
import unq.dapp.grupoj.soccergenius.util.InputSanitizer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("unit")
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TeamServiceTest {
    @Mock
    private TeamScrapingService webScrapingService;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private Mapper mapper;
    @Mock
    private FootballDataApiService footballDataApiService;
    @InjectMocks
    private TeamServiceImpl teamService;

    // Tests for getTeamPlayers
    @Test
    void getTeamPlayers_Success() {
        String teamName = "FC Barcelona";
        String country = "Spain";
        List<String> mockPlayerIds = Arrays.asList("player1", "player2");

        // Mock Scrapping
        when(webScrapingService.getPlayersNamesFromTeam(anyString(), anyString())).thenReturn(mockPlayerIds);

        List<String> playerIds = teamService.getTeamPlayers(teamName, country);

        // Assert
        assertNotNull(playerIds);
        assertEquals(mockPlayerIds, playerIds);
        verify(webScrapingService).getPlayersNamesFromTeam(InputSanitizer.sanitizeInput(teamName), InputSanitizer.sanitizeInput(country));
    }

    @Test
    void getTeamPlayers_ScrappingError() {
        String teamName = "FC Barcelona";
        String country = "Spain";

        // Mock scraping failure
        when(webScrapingService.getPlayersNamesFromTeam(anyString(), anyString()))
            .thenThrow(new ScrappingException("Error scraping players"));

        // Assert exception is thrown
        ScrappingException exception = assertThrows(ScrappingException.class, () -> teamService.getTeamPlayers(teamName, country));

        assertEquals("Error scraping players", exception.getMessage());
        verify(webScrapingService).getPlayersNamesFromTeam(InputSanitizer.sanitizeInput(teamName), InputSanitizer.sanitizeInput(country));
    }

    // Tests for getUpcomingMatches
    @Test
    void getUpcomingMatches_Success() {
        String teamNameInput = "FC Barcelona";
        String sanitizedTeamName = InputSanitizer.sanitizeInput(teamNameInput);

        // Create mock data for FootballDataMatchsDto
        FootballDataTeamDto homeTeam1 = new FootballDataTeamDto(1, "FC Barcelona", "Barca", "FCB", "https://crests.football-data.org/fcb.png");
        FootballDataTeamDto awayTeam1 = new FootballDataTeamDto(2, "Real Madrid", "Real", "RMA", "https://crests.football-data.org/rma.png");
        FootballDataMatchDto match1 = new FootballDataMatchDto("2025-07-01T20:00:00Z", homeTeam1, awayTeam1, null);

        FootballDataTeamDto homeTeam2 = new FootballDataTeamDto(3, "Atletico Madrid", "Atleti", "ATM", "https://crests.football-data.org/atm.png");
        FootballDataTeamDto awayTeam2 = new FootballDataTeamDto(1, "FC Barcelona", "Barca", "FCB", "https://crests.football-data.org/fcb.png");
        FootballDataMatchDto match2 = new FootballDataMatchDto("2025-07-15T19:30:00Z", homeTeam2, awayTeam2, null);

        List<FootballDataMatchDto> matches = Arrays.asList(match1, match2);
        FootballDataMatchsDto mockResponse = mock(FootballDataMatchsDto.class);
        when(mockResponse.getMatches()).thenReturn(matches);

        // Mock API call
        when(footballDataApiService.getUpcomingMatchesFromTeam(sanitizedTeamName)).thenReturn(mockResponse);

        // Call service method
        List<MatchDTO> result = teamService.getUpcomingMatches(teamNameInput);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Check first match details
        MatchDTO firstMatch = result.getFirst();
        assertEquals("FC Barcelona", firstMatch.getLocalTeam());
        assertEquals("Real Madrid", firstMatch.getVisitorTeam());
        assertEquals("La Liga", firstMatch.getCompetition());
        assertEquals("2025-07-01T20:00:00Z", firstMatch.getUtcDate());

        // Check second match details
        MatchDTO secondMatch = result.get(1);
        assertEquals("Atletico Madrid", secondMatch.getLocalTeam());
        assertEquals("FC Barcelona", secondMatch.getVisitorTeam());
        assertEquals("La Liga", secondMatch.getCompetition());
        assertEquals("2025-07-15T19:30:00Z", secondMatch.getUtcDate());

        // Verify calls
        verify(footballDataApiService).getUpcomingMatchesFromTeam(sanitizedTeamName);
    }

    @Test
    void getUpcomingMatches_ThrowErrorWithUnknownTeamNamesAndCompetition() {
        // Arrange
        String teamNameInput = "FC Test";
        when(footballDataApiService.getUpcomingMatchesFromTeam(anyString()))
                .thenThrow(new IllegalArgumentException("Unknown team name"));
        // Act and Assert
        assertThrows(IllegalArgumentException.class, () ->
                teamService.getUpcomingMatches(teamNameInput), "Should throw IllegalArgumentException when team name is unknown");
    }

    // Tests for getTeamFromLaLigaById
    @Test
    void getTeamFromLaLigaById_FoundInDb() {
        int teamId = 1;
        Team mockDbTeam = new Team();
        mockDbTeam.setId(teamId);
        mockDbTeam.setName("Real Madrid");
        TeamDto mockTeamDto = new TeamDto(Integer.toString(teamId), "Real Madrid", null);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(mockDbTeam));
        when(mapper.toDTO(mockDbTeam)).thenReturn(mockTeamDto);

        TeamDto resultDto = teamService.getTeamFromLaLigaById(teamId);

        assertNotNull(resultDto);
        verify(teamRepository, times(1)).findById(teamId);
        verify(mapper, times(1)).toDTO(mockDbTeam);
        verify(webScrapingService, never()).scrapTeamDataById(anyInt());
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    void getTeamFromLaLigaById_NotFoundInDb_ScrapedSuccessfully() {
        int teamId = 2;
        Team mockScrapedTeam = new Team();
        mockScrapedTeam.setId(teamId);
        mockScrapedTeam.setName("FC Barcelona");
        TeamDto mockTeamDto = new TeamDto(Integer.toString(teamId), "FC Barcelona", null);

        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());
        when(webScrapingService.scrapTeamDataById(teamId)).thenReturn(mockScrapedTeam);
        when(teamRepository.save(mockScrapedTeam)).thenReturn(mockScrapedTeam); // Assuming save returns the saved entity
        when(mapper.toDTO(mockScrapedTeam)).thenReturn(mockTeamDto);

        TeamDto resultDto = teamService.getTeamFromLaLigaById(teamId);

        assertNotNull(resultDto);
        verify(teamRepository, times(1)).findById(teamId);
        verify(webScrapingService, times(1)).scrapTeamDataById(teamId);
        verify(teamRepository, times(1)).save(mockScrapedTeam);
        verify(mapper, times(1)).toDTO(mockScrapedTeam);
    }

    @Test
    void getTeamFromLaLigaById_NotFoundInDb_ScrapingFails() {
        int teamId = 3;
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());
        when(webScrapingService.scrapTeamDataById(teamId)).thenThrow(new ScrappingException("Failed to scrape team"));

        ScrappingException exception = assertThrows(ScrappingException.class, () -> teamService.getTeamFromLaLigaById(teamId));

        assertEquals("Failed to scrape team", exception.getMessage());
        verify(teamRepository, times(1)).findById(teamId);
        verify(webScrapingService, times(1)).scrapTeamDataById(teamId);
        verify(teamRepository, never()).save(any(Team.class));
        verify(mapper, never()).toDTO(any(Team.class));
    }

    @Test
    void getTeamsComparison_WithInvalidTeamA_ShouldThrowError() {
        String teamAName = "Inter";
        String teamBName = "Barcelona";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                teamService.getTeamsComparison(teamAName, teamBName));

        assertEquals("No LaLiga team found for: " + teamAName.toLowerCase(), exception.getMessage());
        verify(webScrapingService, times(0)).scrapTeamStatisticsById(anyInt());
    }

    @Test
    void getTeamsComparison_WithInvalidTeamB_ShouldThrowError() {
        String teamAName = "Barcelona";
        String teamBName = "Miami Inter";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                teamService.getTeamsComparison(teamAName, teamBName));

        assertEquals("No LaLiga team found for: " + teamBName.toLowerCase(), exception.getMessage());
        verify(webScrapingService, times(0)).scrapTeamStatisticsById(anyInt());
    }

    @Test
    void getTeamsComparison_WithSameTeam_ShouldThrowError() {
        String teamAName = "Barcelona";
        String teamBName = "Barcelona";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                teamService.getTeamsComparison(teamAName, teamBName));

        assertEquals("Cannot compare a team with itself", exception.getMessage());
        verify(webScrapingService, times(0)).scrapTeamStatisticsById(anyInt());
    }

    @Test
    void getTeamsComparison_WithEmptyTeamA_ShouldThrowError() {
        String teamAName = "";
        String teamBName = "Barcelona";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                teamService.getTeamsComparison(teamAName, teamBName));

        assertEquals("Team names cannot be null or empty", exception.getMessage());
        verify(webScrapingService, times(0)).scrapTeamStatisticsById(anyInt());
    }

    @Test
    void getTeamsComparison_WithEmptyTeamB_ShouldThrowError() {
        String teamAName = "Real Madrid";
        String teamBName = "";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                teamService.getTeamsComparison(teamAName, teamBName));

        assertEquals("Team names cannot be null or empty", exception.getMessage());
        verify(webScrapingService, times(0)).scrapTeamStatisticsById(anyInt());
    }

}
