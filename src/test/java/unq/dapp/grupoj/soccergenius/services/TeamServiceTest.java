package unq.dapp.grupoj.soccergenius.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
import unq.dapp.grupoj.soccergenius.mappers.Mapper;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.model.dtos.CompetitionDTO;
import unq.dapp.grupoj.soccergenius.model.dtos.FootballApiResponseDTO;
import unq.dapp.grupoj.soccergenius.model.dtos.MatchDTO;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamDto;
import unq.dapp.grupoj.soccergenius.repository.TeamRepository;
import unq.dapp.grupoj.soccergenius.services.external.whoScored.WebScrapingService;
import unq.dapp.grupoj.soccergenius.services.player.PlayerService;
import unq.dapp.grupoj.soccergenius.services.team.TeamServiceImpl;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {
    @Mock
    private WebScrapingService webScrapingService;

    @Mock
    private PlayerService playerService; // Injected but not used by the methods under test

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private TeamServiceImpl teamService;

    private String teamName;
    private String country;
    private String sanitizedTeamName;
    private String sanitizedCountry;

    @BeforeEach
    void setUp() {
        teamName = "River Plate\n";
        country = "Argentina\r";
        sanitizedTeamName = "River Plate_";
        sanitizedCountry = "Argentina_";
    }

    // Tests for getTeamPlayers
    @Test
    void getTeamPlayers_Success() {
        List<String> mockPlayerIds = Arrays.asList("player1", "player2");
        when(webScrapingService.getPlayersIdsFromTeam(sanitizedTeamName, sanitizedCountry))
                .thenReturn(mockPlayerIds);

        List<String> playerIds = teamService.getTeamPlayers(teamName, country);

        assertNotNull(playerIds);
        assertEquals(2, playerIds.size());
        assertEquals(mockPlayerIds, playerIds);
        verify(webScrapingService, times(1)).getPlayersIdsFromTeam(sanitizedTeamName, sanitizedCountry);
    }

    @Test
    void getTeamPlayers_ScrappingError() {
        when(webScrapingService.getPlayersIdsFromTeam(sanitizedTeamName, sanitizedCountry))
                .thenThrow(new RuntimeException("Scraping failed"));

        ScrappingException exception = assertThrows(ScrappingException.class, () -> {
            teamService.getTeamPlayers(teamName, country);
        });

        assertEquals("Scraping failed", exception.getMessage());
        verify(webScrapingService, times(1)).getPlayersIdsFromTeam(sanitizedTeamName, sanitizedCountry);
    }

    // Tests for getUpcomingMatches
    @Test
    void getUpcomingMatches_Success() {
        String teamNameInput = "FC Barcelona";
        int teamId = 123;

        // Mocking first API call (get teams from competition)
        Team teamInCompetition = new Team();
        teamInCompetition.setId(teamId);
        teamInCompetition.setName("FC Barcelona");
        List<Team> teamsList = Collections.singletonList(teamInCompetition);

        // Assuming CompetitionDTO constructor takes (param1, param2, List<Team>)
        // You'll need to replace null with actual objects or mocks if the constructor
        // requires non-null values for the first two parameters and they are used.
        // For this test's purpose, if only the 'teams' list is relevant from CompetitionDTO,
        // nulls for other parameters might be acceptable.
        CompetitionDTO competitionDTO = new CompetitionDTO(null, null, teamsList); // Adjusted instantiation

        ResponseEntity<CompetitionDTO> competitionResponseEntity = new ResponseEntity<>(competitionDTO, HttpStatus.OK);
        when(restTemplate.exchange(
                eq("https://api.football-data.org/v4/competitions/2014/teams"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(CompetitionDTO.class)))
                .thenReturn(competitionResponseEntity);

        // Mocking second API call (get matches for the team)
        FootballApiResponseDTO apiResponseDTO = new FootballApiResponseDTO();

        // Simulate raw DTOs from API (unq.dapp.grupoj.soccergenius.model.footballApi.MatchDTO)
        // These raw DTOs still use ZonedDateTime, as the service method is responsible for conversion.
        MatchDTO rawMatch1 = new MatchDTO();
        rawMatch1.setLocalTeam("FC Barcelona");
        rawMatch1.setVisitorTeam("Real Madrid");
        rawMatch1.setCompetition("La Liga");

        ZonedDateTime zonedDateTime = ZonedDateTime.now().plusDays(7);
        String dateAsString = zonedDateTime.toString();
        rawMatch1.setUtcDate(dateAsString);

        MatchDTO rawMatch2 = new MatchDTO();
        rawMatch2.setLocalTeam("Valencia CF");
        rawMatch2.setVisitorTeam("FC Barcelona");
        rawMatch2.setCompetition("La Liga");

        zonedDateTime = ZonedDateTime.now().plusDays(14);
        dateAsString = zonedDateTime.toString();
        rawMatch2.setUtcDate(dateAsString);

        apiResponseDTO.setMatches(Arrays.asList(rawMatch1, rawMatch2));
        ResponseEntity<FootballApiResponseDTO> matchesResponseEntity = new ResponseEntity<>(apiResponseDTO, HttpStatus.OK);
        when(restTemplate.exchange(
                eq("https://api.football-data.org/v4/teams/" + teamId + "/matches?status=SCHEDULED"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(FootballApiResponseDTO.class)))
                .thenReturn(matchesResponseEntity);

        // Act
        List<MatchDTO> upcomingMatches = teamService.getUpcomingMatches(teamNameInput);

        // Assert
        assertNotNull(upcomingMatches);
        assertEquals(2, upcomingMatches.size());
        // Assertions for MatchDTO properties (localTeam, visitorTeam, competition)
        // If MatchDTO now stores date as String, and you needed to assert it,
        // you would compare against the expected string representation.
        // The current assertions don't check the date.
        assertEquals("FC Barcelona", upcomingMatches.getFirst().getLocalTeam());
        assertEquals("Real Madrid", upcomingMatches.getFirst().getVisitorTeam());
        assertEquals("La Liga", upcomingMatches.getFirst().getCompetition());

        verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void getUpcomingMatches_SuccessWithUnknownTeamNamesAndCompetition() {
        String teamNameInput = "FC Test";
        int teamId = 789;

        // Mocking first API call (get teams from competition)
        ResponseEntity<CompetitionDTO> competitionResponseEntity = getCompetitionDTOResponseEntity(teamId);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(CompetitionDTO.class)))
                .thenReturn(competitionResponseEntity);

        // Mocking second API call (get matches for the team)
        FootballApiResponseDTO apiResponseDTO = new FootballApiResponseDTO();

        MatchDTO rawMatch = new MatchDTO();
        rawMatch.setLocalTeam(null); // Simulate null from API
        rawMatch.setVisitorTeam(null); // Simulate null from API
        rawMatch.setCompetition(null); // Simulate null from API

        ZonedDateTime zonedDateTime = ZonedDateTime.now().plusDays(5);
        String dateAsString = zonedDateTime.toString();
        rawMatch.setUtcDate(dateAsString);

        apiResponseDTO.setMatches(Collections.singletonList(rawMatch));
        ResponseEntity<FootballApiResponseDTO> matchesResponseEntity = new ResponseEntity<>(apiResponseDTO, HttpStatus.OK);
        when(restTemplate.exchange(contains("/teams/" + teamId + "/matches"), eq(HttpMethod.GET), any(HttpEntity.class), eq(FootballApiResponseDTO.class)))
                .thenReturn(matchesResponseEntity);

        // Act
        List<MatchDTO> upcomingMatches = teamService.getUpcomingMatches(teamNameInput);

        // Assert
        assertNotNull(upcomingMatches);
        assertEquals(1, upcomingMatches.size());
        assertEquals("Unknown", upcomingMatches.getFirst().getLocalTeam());
        assertEquals("Unknown", upcomingMatches.getFirst().getVisitorTeam());
        assertEquals("Unknown", upcomingMatches.getFirst().getCompetition());
    }

    private static ResponseEntity<CompetitionDTO> getCompetitionDTOResponseEntity(int teamId) {
        Team teamInCompetition = new Team();
        teamInCompetition.setId(teamId);
        teamInCompetition.setName("FC Test"); // Exact match for simplicity
        List<Team> teamsList = Collections.singletonList(teamInCompetition);
        String dummyCompetitionName = "Test Competition";
        String dummyCompetitionId = "COMP_TEST_001";
        CompetitionDTO competitionDTO = new CompetitionDTO(dummyCompetitionName, dummyCompetitionId, teamsList);

        return new ResponseEntity<>(competitionDTO, HttpStatus.OK);
    }

    @Test
    void getUpcomingMatches_TeamNotFoundInCompetition() {
        String teamNameInput = "Unknown Team";
        CompetitionDTO competitionDTO = new CompetitionDTO();
        competitionDTO.setTeams(Collections.emptyList()); // No teams, or teams that don't match
        ResponseEntity<CompetitionDTO> competitionResponseEntity = new ResponseEntity<>(competitionDTO, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("https://api.football-data.org/v4/competitions/2014/teams"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(CompetitionDTO.class)))
                .thenReturn(competitionResponseEntity);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teamService.getUpcomingMatches(teamNameInput);
        });

        assertEquals("Equipo no encontrado: " + teamNameInput, exception.getMessage());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(CompetitionDTO.class));
    }

    @Test
    void getUpcomingMatches_ApiReturnsNullMatches() {
        String teamNameInput = "FC Barcelona";
        int teamId = 123;

        CompetitionDTO competitionDTO = new CompetitionDTO();
        Team teamInCompetition = new Team();
        teamInCompetition.setId(teamId);
        teamInCompetition.setName("FC Barcelona");
        competitionDTO.setTeams(Collections.singletonList(teamInCompetition));
        ResponseEntity<CompetitionDTO> competitionResponseEntity = new ResponseEntity<>(competitionDTO, HttpStatus.OK);
        when(restTemplate.exchange(
                eq("https://api.football-data.org/v4/competitions/2014/teams"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(CompetitionDTO.class)))
                .thenReturn(competitionResponseEntity);

        FootballApiResponseDTO apiResponseDTO = new FootballApiResponseDTO();
        apiResponseDTO.setMatches(null); // Simulate API returning null for matches list
        ResponseEntity<FootballApiResponseDTO> matchesResponseEntity = new ResponseEntity<>(apiResponseDTO, HttpStatus.OK);
        when(restTemplate.exchange(
                eq("https://api.football-data.org/v4/teams/" + teamId + "/matches?status=SCHEDULED"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(FootballApiResponseDTO.class)))
                .thenReturn(matchesResponseEntity);

        List<MatchDTO> upcomingMatches = teamService.getUpcomingMatches(teamNameInput);

        assertNotNull(upcomingMatches);
        assertTrue(upcomingMatches.isEmpty());
    }

    @Test
    void getUpcomingMatches_ApiReturnsNullBodyForMatches() {
        String teamNameInput = "FC Barcelona";
        int teamId = 123;

        CompetitionDTO competitionDTO = new CompetitionDTO();
        Team teamInCompetition = new Team();
        teamInCompetition.setId(teamId);
        teamInCompetition.setName("FC Barcelona");
        competitionDTO.setTeams(Collections.singletonList(teamInCompetition));
        ResponseEntity<CompetitionDTO> competitionResponseEntity = new ResponseEntity<>(competitionDTO, HttpStatus.OK);
        when(restTemplate.exchange(
                eq("https://api.football-data.org/v4/competitions/2014/teams"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(CompetitionDTO.class)))
                .thenReturn(competitionResponseEntity);

        ResponseEntity<FootballApiResponseDTO> matchesResponseEntity = new ResponseEntity<>(null, HttpStatus.OK); // Null body
        when(restTemplate.exchange(
                eq("https://api.football-data.org/v4/teams/" + teamId + "/matches?status=SCHEDULED"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(FootballApiResponseDTO.class)))
                .thenReturn(matchesResponseEntity);

        List<MatchDTO> upcomingMatches = teamService.getUpcomingMatches(teamNameInput);

        assertNotNull(upcomingMatches);
        assertTrue(upcomingMatches.isEmpty());
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
//        assertEquals("FC Barcelona", resultDto.getName());
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

        ScrappingException exception = assertThrows(ScrappingException.class, () -> {
            teamService.getTeamFromLaLigaById(teamId);
        });

        assertEquals("Failed to scrape team", exception.getMessage());
        verify(teamRepository, times(1)).findById(teamId);
        verify(webScrapingService, times(1)).scrapTeamDataById(teamId);
        verify(teamRepository, never()).save(any(Team.class));
        verify(mapper, never()).toDTO(any(Team.class));
    }
}
