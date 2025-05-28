package unq.dapp.grupoj.soccergenius.unit.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.*;
import org.springframework.test.context.ActiveProfiles;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
import unq.dapp.grupoj.soccergenius.exceptions.TeamNotFoundException;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.services.external.whoscored.TeamScrapingService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("e2e")
@Tag("e2e")
@ExtendWith(MockitoExtension.class)
public class TeamScrapingServiceTest {

    @Mock
    private WebDriver mockDriver;

    @Spy
    private TeamScrapingService teamScrapingService = new TeamScrapingService();

    // Mocks for WebElements
    @Mock
    private WebElement mockDivResult;
    @Mock
    private WebElement mockTeamsTable;
    @Mock
    private WebElement mockTbody;
    @Mock
    private WebElement mockTeamRow;
    @Mock
    private WebElement mockLinkEquipo;
    @Mock
    private WebElement mockSpanPais;
    @Mock
    private WebElement mockPlayerListElement;
    @Mock
    private WebElement mockPlayerRowElement;
    @Mock
    private WebElement mockPlayerLinkElement;
    @Mock
    private WebElement mockPlayerIdElement;
    @Mock
    private WebElement mockTableBodyStandings;
    @Mock
    private WebElement mockStandingsRow;
    @Mock
    private WebElement mockFirstCell;
    @Mock
    private WebElement mockPositionSpan;
    @Mock
    private WebElement mockRatingStrongElement;
    @Mock
    private WebElement mockTeamNameSpan;
    @Mock
    private WebElement mockLeagueHref;
    @Mock
    private WebElement mockCountrySpan;
    @Mock
    private WebElement mockTeamLinkElement;


    private final String TEAM_NAME = "FC Barcelona";
    private final String TEAM_COUNTRY = "Spain";
    private final String TEAM_URL = "http://example.com/team/fcbarcelona";
    private final int TEAM_ID = 101;
    private final int PLAYER_ID = 789;

    @BeforeEach
    void setUp() {
        lenient().doReturn(mockDriver).when(teamScrapingService).setupDriverAndNavigate(anyString());
    }

    @Test
    void getPlayersIdsFromTeam_shouldThrowTeamNotFoundException_whenTeamNotInSearchResults() {
        // Arrange
        when(mockDriver.findElement(By.className("search-result"))).thenReturn(mockDivResult);
        when(mockDivResult.findElement(By.xpath("./table[1]"))).thenReturn(mockTeamsTable);
        when(mockTeamsTable.findElement(By.tagName("tbody"))).thenReturn(mockTbody);
        when(mockTbody.findElements(By.xpath("./tr[position()>1]"))).thenReturn(Collections.singletonList(mockTeamRow));

        when(mockTeamRow.findElement(By.xpath("./td[1]/a"))).thenReturn(mockLinkEquipo);
        when(mockLinkEquipo.getText()).thenReturn("Other Team"); // Different team name
        when(mockTeamRow.findElement(By.xpath("./td[2]/span"))).thenReturn(mockSpanPais);
        when(mockSpanPais.getText()).thenReturn(TEAM_COUNTRY);

        // Act & Assert
        TeamNotFoundException exception = assertThrows(TeamNotFoundException.class, () ->
                teamScrapingService.getPlayersIdsFromTeam(TEAM_NAME, TEAM_COUNTRY)
        );
        assertTrue(exception.getMessage().contains("Team " + TEAM_NAME + " not found in country " + TEAM_COUNTRY));
        verify(mockDriver).quit();
    }

    @Test
    void getPlayersIdsFromTeam_shouldThrowScrappingException_onSeleniumError() {
        // Arrange
        when(mockDriver.findElement(By.className("search-result"))).thenThrow(new NoSuchElementException("Search error"));

        // Act & Assert
        // The generic catch (Exception e) in the SUT will wrap this into a ScrappingException,
        // but the original method doesn't have a generic catch, so it will be a raw Selenium exception.
        // Let's adjust the SUT or the test. The SUT's finally block handles driver.quit().
        // The method getPlayersIdsFromTeam does not have a general catch block, so NoSuchElementException will propagate.
        assertThrows(NoSuchElementException.class, () ->
                teamScrapingService.getPlayersIdsFromTeam(TEAM_NAME, TEAM_COUNTRY)
        );
        verify(mockDriver).quit();
    }

    // Tests for getCurrentPositionOnLeague
    @Test
    void getCurrentPositionOnLeague_shouldReturnPosition_whenTeamIsFound() {
        // Arrange
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenReturn(mockTableBodyStandings);
        when(mockTableBodyStandings.findElements(By.tagName("tr"))).thenReturn(Collections.singletonList(mockStandingsRow));
        when(mockStandingsRow.getAttribute("data-team-id")).thenReturn(String.valueOf(TEAM_ID));
        when(mockStandingsRow.findElement(By.xpath("./td[1]"))).thenReturn(mockFirstCell);
        when(mockFirstCell.findElement(By.tagName("span"))).thenReturn(mockPositionSpan);
        when(mockPositionSpan.getText()).thenReturn("3");

        // Act
        int position = teamScrapingService.getCurrentPositionOnLeague(TEAM_ID);

        // Assert
        assertEquals(3, position);
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentPositionOnLeague_shouldThrowTeamNotFoundException_whenTeamNotInStandings() {
        // Arrange
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenReturn(mockTableBodyStandings);
        when(mockTableBodyStandings.findElements(By.tagName("tr"))).thenReturn(Collections.singletonList(mockStandingsRow));
        when(mockStandingsRow.getAttribute("data-team-id")).thenReturn("999"); // Different team ID

        // Act & Assert
        TeamNotFoundException exception = assertThrows(TeamNotFoundException.class, () ->
                teamScrapingService.getCurrentPositionOnLeague(TEAM_ID)
        );
        assertTrue(exception.getMessage().contains("Team ID " + TEAM_ID + " not found in league standings"));
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentPositionOnLeague_shouldThrowScrappingException_whenPositionTextIsInvalid() {
        // Arrange
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenReturn(mockTableBodyStandings);
        when(mockTableBodyStandings.findElements(By.tagName("tr"))).thenReturn(Collections.singletonList(mockStandingsRow));
        when(mockStandingsRow.getAttribute("data-team-id")).thenReturn(String.valueOf(TEAM_ID));
        when(mockStandingsRow.findElement(By.xpath("./td[1]"))).thenReturn(mockFirstCell);
        when(mockFirstCell.findElement(By.tagName("span"))).thenReturn(mockPositionSpan);
        when(mockPositionSpan.getText()).thenReturn("NotANumber");

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.getCurrentPositionOnLeague(TEAM_ID)
        );
        assertTrue(exception.getMessage().contains("Error parsing position text 'NotANumber'"));
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentPositionOnLeague_shouldThrowTeamNotFoundException_whenPositionTextIsEmpty() {
        // Arrange
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenReturn(mockTableBodyStandings);
        when(mockTableBodyStandings.findElements(By.tagName("tr"))).thenReturn(Collections.singletonList(mockStandingsRow));
        when(mockStandingsRow.getAttribute("data-team-id")).thenReturn(String.valueOf(TEAM_ID));
        when(mockStandingsRow.findElement(By.xpath("./td[1]"))).thenReturn(mockFirstCell);
        when(mockFirstCell.findElement(By.tagName("span"))).thenReturn(mockPositionSpan);
        when(mockPositionSpan.getText()).thenReturn("  "); // Empty after trim

        // Act & Assert
        TeamNotFoundException exception = assertThrows(TeamNotFoundException.class, () ->
                teamScrapingService.getCurrentPositionOnLeague(TEAM_ID)
        );
        assertTrue(exception.getMessage().contains("Team ID " + TEAM_ID + " found, but has no position text"));
        verify(mockDriver).quit();
    }


    @Test
    void getCurrentPositionOnLeague_shouldThrowScrappingException_onGeneralSeleniumError() {
        // Arrange
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenThrow(new TimeoutException("Timeout waiting for table"));

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.getCurrentPositionOnLeague(TEAM_ID)
        );
        assertTrue(exception.getMessage().contains("Error scraping current position on league"));
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentRankingOfTeam_shouldThrowScrappingException_onSeleniumError() {
        // Arrange
        String ratingXPath = "//tbody[@id='top-team-stats-summary-content']/tr[last()]/td[@class='rating']/strong";
        when(mockDriver.findElement(By.xpath(ratingXPath))).thenThrow(new TimeoutException("Timeout"));

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.getCurrentRankingOfTeam(TEAM_ID)
        );
        assertTrue(exception.getMessage().contains("Error scraping current ranking of team"));
        verify(mockDriver).quit();
    }


    // Tests for scrapTeamDataById
    @Test
    void scrapTeamDataById_shouldReturnTeam_whenDataIsValid() {
        // Arrange
        when(mockDriver.findElement(By.className("team-header-name"))).thenReturn(mockTeamNameSpan);
        when(mockTeamNameSpan.getText()).thenReturn(TEAM_NAME);
        when(mockDriver.findElement(By.cssSelector("#breadcrumb-nav a"))).thenReturn(mockLeagueHref);
        when(mockLeagueHref.getText()).thenReturn("La Liga");
        when(mockDriver.findElement(By.cssSelector(".iconize.iconize-icon-left"))).thenReturn(mockCountrySpan);
        when(mockCountrySpan.getText()).thenReturn(TEAM_COUNTRY);

        // Act
        Team team = teamScrapingService.scrapTeamDataById(TEAM_ID);

        // Assert
        assertNotNull(team);
        assertEquals(TEAM_ID, team.getId());
        assertEquals(TEAM_NAME, team.getName());
        assertEquals(TEAM_COUNTRY, team.getCountry());
        assertEquals("La Liga", team.getLeague());
        verify(mockDriver).quit();
    }

    @Test
    void scrapTeamDataById_shouldThrowSeleniumException_onElementError() {
        // Arrange
        when(mockDriver.findElement(By.className("team-header-name"))).thenThrow(new NoSuchElementException("Error finding team name"));

        // Act & Assert
        // This method does not wrap Selenium exceptions in ScrappingException within its try block
        assertThrows(NoSuchElementException.class, () ->
                teamScrapingService.scrapTeamDataById(TEAM_ID)
        );
        verify(mockDriver).quit(); // quit() is called in finally
    }

    // Tests for scrapActualTeamFromPlayer
    @Test
    void scrapActualTeamFromPlayer_shouldReturnTeamId_whenDataIsValid() {
        // Arrange
        when(mockDriver.findElement(By.className("team-link"))).thenReturn(mockTeamLinkElement);
        when(mockTeamLinkElement.getAttribute("href")).thenReturn("/teams/" + TEAM_ID + "/some-team-name");

        // Act
        int actualTeamId = teamScrapingService.scrapActualTeamFromPlayer(PLAYER_ID);

        // Assert
        assertEquals(TEAM_ID, actualTeamId);
        verify(mockDriver).quit();
    }

    @Test
    void scrapActualTeamFromPlayer_shouldThrowScrappingException_whenHrefIsInvalidFormat() {
        // Arrange
        when(mockDriver.findElement(By.className("team-link"))).thenReturn(mockTeamLinkElement);
        when(mockTeamLinkElement.getAttribute("href")).thenReturn("/teams/not_an_id/some-team-name");

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.scrapActualTeamFromPlayer(PLAYER_ID)
        );
        assertTrue(exception.getMessage().contains("Error scraping team data")); // Wraps NumberFormatException
        verify(mockDriver).quit();
    }

    @Test
    void scrapActualTeamFromPlayer_shouldThrowScrappingException_onSeleniumError() {
        // Arrange
        when(mockDriver.findElement(By.className("team-link"))).thenThrow(new NoSuchElementException("Error finding team link"));

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.scrapActualTeamFromPlayer(PLAYER_ID)
        );
        assertTrue(exception.getMessage().contains("Error scraping team data"));
        verify(mockDriver).quit();
    }

    @Test
    void setupDriverAndNavigate_shouldBeCalledAndQuit_evenIfScrapingFailsMidway() {
        // Test for a method that might fail after setupDriverAndNavigate
        // Example: getCurrentPositionOnLeague
        doReturn(mockDriver).when(teamScrapingService).setupDriverAndNavigate(anyString());
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenThrow(new NoSuchElementException("Table not found"));

        assertThrows(ScrappingException.class, () -> teamScrapingService.getCurrentPositionOnLeague(TEAM_ID));

        verify(teamScrapingService).setupDriverAndNavigate(anyString());
        verify(mockDriver).quit();
    }

    @Test
    void setupDriverAndNavigate_shouldNotCallQuit_ifSetupItselfFails() {
        // Test for a method where setupDriverAndNavigate fails
        doThrow(new RuntimeException("Failed to setup driver")).when(teamScrapingService).setupDriverAndNavigate(anyString());

        // Example: getCurrentPositionOnLeague
        // The specific exception might vary based on how WebScrapingService handles it,
        // but the key is that quit() on mockDriver shouldn't be called if mockDriver was never returned.
        assertThrows(ScrappingException.class, () -> teamScrapingService.getCurrentPositionOnLeague(TEAM_ID));

        verify(teamScrapingService).setupDriverAndNavigate(anyString());
        verify(mockDriver, never()).quit(); // Because driver would be null or setup failed before assignment
    }
}
