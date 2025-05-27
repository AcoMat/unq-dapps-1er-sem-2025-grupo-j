package unq.dapp.grupoj.soccergenius.services;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import unq.dapp.grupoj.soccergenius.exceptions.FootballDataApiException;
import unq.dapp.grupoj.soccergenius.model.dtos.external.football_data.FootballDataMatchsDto;
import unq.dapp.grupoj.soccergenius.services.external.football_data.FootballDataApiService;
import unq.dapp.grupoj.soccergenius.services.external.football_data.TeamIdMappingUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FootballDataApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    // We will create the instance manually to inject the mocked RestTemplate
    // and to ensure TeamIdMappingUtil static mock is active during construction.
    private FootballDataApiService footballDataApiService;

    private static MockedStatic<TeamIdMappingUtil> mockedTeamIdMappingUtil;
    private static MockedStatic<LocalDate> mockedLocalDate;
    private static Map<Integer, Integer> mockTeamIdMap;
    private static final LocalDate FIXED_NOW = LocalDate.of(2024, 7, 21); // Fixed date for consistent tests

    @BeforeAll
    static void beforeAll() {
        // Mock TeamIdMappingUtil.getWhoScoredToFootballDataTeamIdMap()
        mockTeamIdMap = new HashMap<>();
        mockTeamIdMap.put(100, 200); // Example: WhoScored ID 100 -> FootballData ID 200
        mockTeamIdMap.put(101, 201);

        mockedTeamIdMappingUtil = Mockito.mockStatic(TeamIdMappingUtil.class);
        mockedTeamIdMappingUtil.when(TeamIdMappingUtil::getWhoScoredToFootballDataTeamIdMap).thenReturn(mockTeamIdMap);

        // Mock LocalDate.now()
        mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
        mockedLocalDate.when(LocalDate::now).thenReturn(FIXED_NOW);
    }

    @AfterAll
    static void afterAll() {
        mockedTeamIdMappingUtil.close();
        mockedLocalDate.close();
    }

    @BeforeEach
    void setUp() {
        // Instantiate the service with the mocked RestTemplate
        footballDataApiService = new FootballDataApiService(restTemplate);
        // Set @Value fields using ReflectionTestUtils
        ReflectionTestUtils.setField(footballDataApiService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(footballDataApiService, "baseUrl", "http://test-api.football-data.org/v4");
    }

    @Test
    void convertWhoScoredIdToFootballDataId_whenMappingExists_shouldReturnFootballDataId() {
        int whoScoredId = 100;
        Integer expectedFootballDataId = 200;

        Integer actualFootballDataId = footballDataApiService.convertWhoScoredIdToFootballDataId(whoScoredId);

        assertEquals(expectedFootballDataId, actualFootballDataId);
    }

    @Test
    void convertWhoScoredIdToFootballDataId_whenMappingDoesNotExist_shouldReturnNull() {
        int whoScoredId = 999; // This ID is not in our mockTeamIdMap

        Integer actualFootballDataId = footballDataApiService.convertWhoScoredIdToFootballDataId(whoScoredId);

        assertNull(actualFootballDataId);
    }

    @Test
    void getLastXMatchesFromTeam_whenValidTeamIdAndApiSucceeds_shouldReturnMatchsDto() {
        int whoScoredTeamId = 100; // Mapped to 200
        int limit = 5;
        Integer footballDataTeamId = 200; // Expected conversion

        FootballDataMatchsDto expectedDto = new FootballDataMatchsDto(); // Mock DTO
        ResponseEntity<FootballDataMatchsDto> mockResponseEntity = new ResponseEntity<>(expectedDto, HttpStatus.OK);

        LocalDate endDate = FIXED_NOW;
        LocalDate startDate = endDate.minusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateFrom = startDate.format(formatter);
        String dateTo = endDate.format(formatter);

        String expectedUrl = "http://test-api.football-data.org/v4/teams/" + footballDataTeamId +
                "/matches?dateFrom=" + dateFrom + "&dateTo=" + dateTo +
                "&limit=" + limit + "&status=FINISHED";

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("X-Auth-Token", "test-api-key");
        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedEntity),
                eq(FootballDataMatchsDto.class)
        )).thenReturn(mockResponseEntity);

        FootballDataMatchsDto actualDto = footballDataApiService.getLastXMatchesFromTeam(whoScoredTeamId, limit);

        assertNotNull(actualDto);
        assertEquals(expectedDto, actualDto);

        verify(restTemplate).exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedEntity),
                eq(FootballDataMatchsDto.class)
        );
    }

    @Test
    void getLastXMatchesFromTeam_whenTeamIdNotMapped_shouldThrowFootballDataApiException() {
        int unmappedWhoScoredTeamId = 999;
        int limit = 5;

        FootballDataApiException exception = assertThrows(FootballDataApiException.class, () -> {
            footballDataApiService.getLastXMatchesFromTeam(unmappedWhoScoredTeamId, limit);
        });

        assertEquals("No Football-data.org ID found for WhoScored team ID: " + unmappedWhoScoredTeamId, exception.getMessage());
        verifyNoInteractions(restTemplate); // Ensure RestTemplate is not called
    }

    @Test
    void getLastXMatchesFromTeam_whenApiCallFails_shouldPropagateException() {
        int whoScoredTeamId = 101; // Mapped to 201
        int limit = 3;
        Integer footballDataTeamId = 201;

        LocalDate endDate = FIXED_NOW;
        LocalDate startDate = endDate.minusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateFrom = startDate.format(formatter);
        String dateTo = endDate.format(formatter);
        String expectedUrl = "http://test-api.football-data.org/v4/teams/" + footballDataTeamId +
                "/matches?dateFrom=" + dateFrom + "&dateTo=" + dateTo +
                "&limit=" + limit + "&status=FINISHED";

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("X-Auth-Token", "test-api-key");
        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        HttpClientErrorException mockHttpException = new HttpClientErrorException(HttpStatus.NOT_FOUND, "Team not found on external API");
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedEntity),
                eq(FootballDataMatchsDto.class)
        )).thenThrow(mockHttpException);

        HttpClientErrorException thrownException = assertThrows(HttpClientErrorException.class, () -> {
            footballDataApiService.getLastXMatchesFromTeam(whoScoredTeamId, limit);
        });

        assertEquals(HttpStatus.NOT_FOUND, thrownException.getStatusCode());
        assertTrue(thrownException.getMessage().contains("Team not found on external API"));

        verify(restTemplate).exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedEntity),
                eq(FootballDataMatchsDto.class)
        );
    }

    @Test
    void getLastXMatchesFromTeam_whenApiReturnsNullBody_shouldReturnNull() {
        int whoScoredTeamId = 100;
        int limit = 5;
        Integer footballDataTeamId = 200;

        ResponseEntity<FootballDataMatchsDto> mockResponseEntityWithNullBody = new ResponseEntity<>(null, HttpStatus.OK);

        LocalDate endDate = FIXED_NOW;
        LocalDate startDate = endDate.minusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateFrom = startDate.format(formatter);
        String dateTo = endDate.format(formatter);
        String expectedUrl = "http://test-api.football-data.org/v4/teams/" + footballDataTeamId +
                "/matches?dateFrom=" + dateFrom + "&dateTo=" + dateTo +
                "&limit=" + limit + "&status=FINISHED";

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("X-Auth-Token", "test-api-key");
        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedEntity),
                eq(FootballDataMatchsDto.class)
        )).thenReturn(mockResponseEntityWithNullBody);

        FootballDataMatchsDto actualDto = footballDataApiService.getLastXMatchesFromTeam(whoScoredTeamId, limit);

        assertNull(actualDto);
    }

}
