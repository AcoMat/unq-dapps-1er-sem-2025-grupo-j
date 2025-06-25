package unq.dapp.grupoj.soccergenius.unit.services;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import unq.dapp.grupoj.soccergenius.exceptions.FootballDataApiException;
import unq.dapp.grupoj.soccergenius.model.dtos.external.football_data.FootballDataMatchsDto;
import unq.dapp.grupoj.soccergenius.services.external.football_data.FootballDataApiService;
import unq.dapp.grupoj.soccergenius.util.InputSanitizer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ActiveProfiles("unit")
@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class FootballDataApiServiceTest {

    @Mock
    private RestTemplate restTemplate;
    private FootballDataApiService footballDataApiService;
    private final String BASE_URL = "http://api.football-data.org/v4";
    private final String API_KEY = "test-api-key";

    @BeforeEach
    public void setup() {
        footballDataApiService = new FootballDataApiService(restTemplate);
        // Set the private fields using ReflectionTestUtils since @Value won't work in unit tests
        ReflectionTestUtils.setField(footballDataApiService, "baseUrl", BASE_URL);
        ReflectionTestUtils.setField(footballDataApiService, "apiKey", API_KEY);
    }

    @Test
    public void testConvertWhoScoredIdToFootballDataId() {
        // Arrange
        int whoScoredTeamId = 52;
        int expectedFootballDataId = 86;
        // Act
        Integer result = footballDataApiService.convertWhoScoredIdToFootballDataId(whoScoredTeamId);
        // Assert
        assertEquals(expectedFootballDataId, result);
    }

    @Test
    public void testGetLastXMatchesFromTeam() {
        // Arrange
        int whoScoredTeamId = 52;
        int limit = 5;
        int footballDataId = 86;
        FootballDataMatchsDto expectedResponse = new FootballDataMatchsDto();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateFrom = startDate.format(formatter);
        String dateTo = endDate.format(formatter);

        String expectedUrl = BASE_URL + "/teams/" + footballDataId + "/matches?dateFrom=" + dateFrom + "&dateTo=" + dateTo + "&limit=" + limit + "&status=FINISHED";

        // Setup response with more specific parameter matching
        ResponseEntity<FootballDataMatchsDto> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
        when(restTemplate.exchange(
            eq(expectedUrl),
            eq(HttpMethod.GET),
            argThat(entity -> {
                HttpHeaders headers = entity.getHeaders();
                return headers.containsKey("X-Auth-Token") &&
                       headers.getFirst("X-Auth-Token").equals(API_KEY);
            }),
            eq(FootballDataMatchsDto.class)
        )).thenReturn(responseEntity);

        // Act
        FootballDataMatchsDto result = footballDataApiService.getLastXMatchesFromTeam(whoScoredTeamId, limit);

        // Assert
        assertEquals(expectedResponse, result);

        // Verify the correct URL and method were used
        verify(restTemplate).exchange(
            eq(expectedUrl),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(FootballDataMatchsDto.class)
        );
    }

    @Test
    public void testGetUpcomingMatchesFromTeam() {
        // Arrange
        String teamName = InputSanitizer.sanitizeInput("Barcelona");
        int footballDataId = 65;
        FootballDataMatchsDto expectedResponse = new FootballDataMatchsDto();
        String expectedUrl = "https://api.football-data.org/v4/teams/" + footballDataId + "/matches?status=SCHEDULED";

        // Setup response
        ResponseEntity<FootballDataMatchsDto> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(FootballDataMatchsDto.class)
        )).thenReturn(responseEntity);

        // Act
        FootballDataMatchsDto result = footballDataApiService.getUpcomingMatchesFromTeam(teamName);

        // Assert
        assertEquals(expectedResponse, result);
    }

    @Test
    public void testGetUpcomingMatchesFromTeam_ThrowsException_WhenApiCallFails() {
        // Arrange
        String teamName = InputSanitizer.sanitizeInput("Barcelona");

        // Setup error response
        ResponseEntity<FootballDataMatchsDto> responseEntity = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(FootballDataMatchsDto.class)
        )).thenReturn(responseEntity);

        // Act & Assert
        Exception exception = assertThrows(FootballDataApiException.class, () -> footballDataApiService.getUpcomingMatchesFromTeam(teamName));
        assertTrue(exception.getMessage().contains("Error fetching upcoming matches for team: " + teamName));
    }

    @Test
    public void testConvertWhoScoredIdToFootballDataId_WithNonLaLigaId() {
        // Arrange
        int nonLaLigaWhoScoredId = 9999; // ID not in LaLiga list
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> footballDataApiService.convertWhoScoredIdToFootballDataId(nonLaLigaWhoScoredId), "Should throw FootballDataApiException when using a non-LaLiga team ID");
    }

    @Test
    public void testGetLastXMatchesFromTeam_WithNonLaLigaId() {
        // Arrange
        int nonLaLigaWhoScoredId = 9999; // ID not in LaLiga list
        int limit = 5;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> footballDataApiService.getLastXMatchesFromTeam(nonLaLigaWhoScoredId, limit), "Should throw IllegalArgumentException when using a non-LaLiga team ID");
        // Verify no HTTP request was made
        verifyNoInteractions(restTemplate);
    }

    @Test
    public void testGetUpcomingMatchesFromTeam_WithNonLaLigaTeamName() {
        // Arrange
        String nonLaLigaTeamName = InputSanitizer.sanitizeInput("Manchester United"); // Not a LaLiga team

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> footballDataApiService.getUpcomingMatchesFromTeam(nonLaLigaTeamName), "Should throw IllegalArgumentException when using a non-LaLiga team name");

        // Verify no HTTP request was made
        verifyNoInteractions(restTemplate);
    }
}
