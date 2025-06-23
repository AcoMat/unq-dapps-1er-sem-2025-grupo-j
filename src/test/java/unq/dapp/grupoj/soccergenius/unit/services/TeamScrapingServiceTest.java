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
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("unit")
@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class TeamScrapingServiceTest {

    @Mock
    private WebDriver mockDriver;

    @Spy
    private TeamScrapingService teamScrapingService = new TeamScrapingService();

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
    private final int TEAM_ID = 101;
    private final int PLAYER_ID = 789;

    @BeforeEach
    void setUp() {
        lenient().doReturn(mockDriver).when(teamScrapingService).setupDriverAndNavigate(anyString());
    }

    @Test
    void getPlayersIdsFromTeam_shouldThrowTeamNotFoundException_whenTeamNotInSearchResults() {
        when(mockDriver.findElement(By.className("search-result"))).thenReturn(mockDivResult);
        when(mockDivResult.findElement(By.xpath("./table[1]"))).thenReturn(mockTeamsTable);
        when(mockTeamsTable.findElement(By.tagName("tbody"))).thenReturn(mockTbody);
        when(mockTbody.findElements(By.xpath("./tr[position()>1]"))).thenReturn(Collections.singletonList(mockTeamRow));
        when(mockTeamRow.findElement(By.xpath("./td[1]/a"))).thenReturn(mockLinkEquipo);
        when(mockLinkEquipo.getText()).thenReturn("Other Team"); // Different team name
        when(mockTeamRow.findElement(By.xpath("./td[2]/span"))).thenReturn(mockSpanPais);
        when(mockSpanPais.getText()).thenReturn(TEAM_COUNTRY);

        TeamNotFoundException exception = assertThrows(TeamNotFoundException.class, () ->
                teamScrapingService.getPlayersIdsFromTeam(TEAM_NAME, TEAM_COUNTRY)
        );
        assertTrue(exception.getMessage().contains("Team " + TEAM_NAME + " not found in country " + TEAM_COUNTRY));
        verify(mockDriver).quit();
    }

    @Test
    void getPlayersIdsFromTeam_shouldThrowScrappingException_onSeleniumError() {
        when(mockDriver.findElement(By.className("search-result"))).thenThrow(new NoSuchElementException("Search error"));
        assertThrows(NoSuchElementException.class, () ->
                teamScrapingService.getPlayersIdsFromTeam(TEAM_NAME, TEAM_COUNTRY)
        );
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentPositionOnLeague_shouldReturnPosition_whenTeamIsFound() {
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenReturn(mockTableBodyStandings);
        when(mockTableBodyStandings.findElements(By.tagName("tr"))).thenReturn(Collections.singletonList(mockStandingsRow));
        when(mockStandingsRow.getAttribute("data-team-id")).thenReturn(String.valueOf(TEAM_ID));
        when(mockStandingsRow.findElement(By.xpath("./td[1]"))).thenReturn(mockFirstCell);
        when(mockFirstCell.findElement(By.tagName("span"))).thenReturn(mockPositionSpan);
        when(mockPositionSpan.getText()).thenReturn("3");

        int position = teamScrapingService.getCurrentPositionOnLeague(TEAM_ID);

        assertEquals(3, position);
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentPositionOnLeague_shouldThrowTeamNotFoundException_whenTeamNotInStandings() {
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenReturn(mockTableBodyStandings);
        when(mockTableBodyStandings.findElements(By.tagName("tr"))).thenReturn(Collections.singletonList(mockStandingsRow));
        when(mockStandingsRow.getAttribute("data-team-id")).thenReturn("999"); // Different team ID

        TeamNotFoundException exception = assertThrows(TeamNotFoundException.class, () ->
                teamScrapingService.getCurrentPositionOnLeague(TEAM_ID)
        );
        assertTrue(exception.getMessage().contains("Team ID " + TEAM_ID + " not found in league standings"));
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentPositionOnLeague_shouldThrowScrappingException_whenPositionTextIsInvalid() {
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenReturn(mockTableBodyStandings);
        when(mockTableBodyStandings.findElements(By.tagName("tr"))).thenReturn(Collections.singletonList(mockStandingsRow));
        when(mockStandingsRow.getAttribute("data-team-id")).thenReturn(String.valueOf(TEAM_ID));
        when(mockStandingsRow.findElement(By.xpath("./td[1]"))).thenReturn(mockFirstCell);
        when(mockFirstCell.findElement(By.tagName("span"))).thenReturn(mockPositionSpan);
        when(mockPositionSpan.getText()).thenReturn("NotANumber");

        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.getCurrentPositionOnLeague(TEAM_ID)
        );
        assertTrue(exception.getMessage().contains("Error parsing position text 'NotANumber'"));
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentPositionOnLeague_shouldThrowTeamNotFoundException_whenPositionTextIsEmpty() {
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenReturn(mockTableBodyStandings);
        when(mockTableBodyStandings.findElements(By.tagName("tr"))).thenReturn(Collections.singletonList(mockStandingsRow));
        when(mockStandingsRow.getAttribute("data-team-id")).thenReturn(String.valueOf(TEAM_ID));
        when(mockStandingsRow.findElement(By.xpath("./td[1]"))).thenReturn(mockFirstCell);
        when(mockFirstCell.findElement(By.tagName("span"))).thenReturn(mockPositionSpan);
        when(mockPositionSpan.getText()).thenReturn("  ");

        TeamNotFoundException exception = assertThrows(TeamNotFoundException.class, () ->
                teamScrapingService.getCurrentPositionOnLeague(TEAM_ID)
        );
        assertTrue(exception.getMessage().contains("Team ID " + TEAM_ID + " found, but has no position text"));
        verify(mockDriver).quit();
    }


    @Test
    void getCurrentPositionOnLeague_shouldThrowScrappingException_onGeneralSeleniumError() {
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenThrow(new TimeoutException("Timeout waiting for table"));

        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.getCurrentPositionOnLeague(TEAM_ID)
        );
        assertTrue(exception.getMessage().contains("Error scraping current position on league"));
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentRankingOfTeam_shouldThrowScrappingException_onSeleniumError() {
        String ratingXPath = "//tbody[@id='top-team-stats-summary-content']/tr[last()]/td[@class='rating']/strong";
        when(mockDriver.findElement(By.xpath(ratingXPath))).thenThrow(new TimeoutException("Timeout"));

        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.getCurrentRankingOfTeam(TEAM_ID)
        );
        assertTrue(exception.getMessage().contains("Error scraping current ranking of team"));
        verify(mockDriver).quit();
    }

    @Test
    void scrapTeamDataById_shouldReturnTeam_whenDataIsValid() {
        when(mockDriver.findElement(By.className("team-header-name"))).thenReturn(mockTeamNameSpan);
        when(mockTeamNameSpan.getText()).thenReturn(TEAM_NAME);
        when(mockDriver.findElement(By.cssSelector("#breadcrumb-nav a"))).thenReturn(mockLeagueHref);
        when(mockLeagueHref.getText()).thenReturn("La Liga");
        when(mockDriver.findElement(By.cssSelector(".iconize.iconize-icon-left"))).thenReturn(mockCountrySpan);
        when(mockCountrySpan.getText()).thenReturn(TEAM_COUNTRY);

        Team team = teamScrapingService.scrapTeamDataById(TEAM_ID);

        assertNotNull(team);
        assertEquals(TEAM_ID, team.getId());
        assertEquals(TEAM_NAME, team.getName());
        assertEquals(TEAM_COUNTRY, team.getCountry());
        assertEquals("La Liga", team.getLeague());
        verify(mockDriver).quit();
    }

    @Test
    void scrapTeamDataById_shouldThrowSeleniumException_onElementError() {
        when(mockDriver.findElement(By.className("team-header-name"))).thenThrow(new NoSuchElementException("Error finding team name"));

        assertThrows(TeamNotFoundException.class, () ->
                teamScrapingService.scrapTeamDataById(TEAM_ID)
        );
        verify(mockDriver).quit();
    }

    @Test
    void scrapActualTeamFromPlayer_shouldReturnTeamId_whenDataIsValid() {
        when(mockDriver.findElement(By.className("team-link"))).thenReturn(mockTeamLinkElement);
        when(mockTeamLinkElement.getAttribute("href")).thenReturn("/teams/" + TEAM_ID + "/some-team-name");

        int actualTeamId = teamScrapingService.scrapActualTeamFromPlayer(PLAYER_ID);

        assertEquals(TEAM_ID, actualTeamId);
        verify(mockDriver).quit();
    }

    @Test
    void scrapActualTeamFromPlayer_shouldThrowScrappingException_whenHrefIsInvalidFormat() {
        when(mockDriver.findElement(By.className("team-link"))).thenReturn(mockTeamLinkElement);
        when(mockTeamLinkElement.getAttribute("href")).thenReturn("/teams/not_an_id/some-team-name");

        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.scrapActualTeamFromPlayer(PLAYER_ID)
        );
        assertTrue(exception.getMessage().contains("Error scraping team data"));
        verify(mockDriver).quit();
    }

    @Test
    void scrapActualTeamFromPlayer_shouldThrowScrappingException_onSeleniumError() {
        when(mockDriver.findElement(By.className("team-link"))).thenThrow(new NoSuchElementException("Error finding team link"));
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.scrapActualTeamFromPlayer(PLAYER_ID)
        );
        assertTrue(exception.getMessage().contains("Error scraping team data"));
        verify(mockDriver).quit();
    }

    @Test
    void setupDriverAndNavigate_shouldBeCalledAndQuit_evenIfScrapingFailsMidway() {
        doReturn(mockDriver).when(teamScrapingService).setupDriverAndNavigate(anyString());
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenThrow(new NoSuchElementException("Table not found"));

        assertThrows(ScrappingException.class, () -> teamScrapingService.getCurrentPositionOnLeague(TEAM_ID));

        verify(teamScrapingService).setupDriverAndNavigate(anyString());
        verify(mockDriver).quit();
    }

    @Test
    void setupDriverAndNavigate_shouldNotCallQuit_ifSetupItselfFails() {
        doThrow(new RuntimeException("Failed to setup driver")).when(teamScrapingService).setupDriverAndNavigate(anyString());
        assertThrows(ScrappingException.class, () -> teamScrapingService.getCurrentPositionOnLeague(TEAM_ID));

        verify(teamScrapingService).setupDriverAndNavigate(anyString());
        verify(mockDriver, never()).quit();
    }
}
