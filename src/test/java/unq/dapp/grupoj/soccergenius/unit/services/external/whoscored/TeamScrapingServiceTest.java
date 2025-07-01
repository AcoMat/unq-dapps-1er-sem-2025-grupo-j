package unq.dapp.grupoj.soccergenius.unit.services.external.whoscored;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.test.context.ActiveProfiles;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
import unq.dapp.grupoj.soccergenius.exceptions.TeamNotFoundException;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamStatisticsDTO;
import unq.dapp.grupoj.soccergenius.services.external.whoscored.TeamScrapingService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("unit")
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TeamScrapingServiceTest {

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
    private WebElement mockTableBodyStandings;
    @Mock
    private WebElement mockStandingsRow;
    @Mock
    private WebElement mockFirstCell;
    @Mock
    private WebElement mockPositionSpan;
    @Mock
    private WebElement mockTeamNameSpan;
    @Mock
    private WebElement mockLeagueHref;
    @Mock
    private WebElement mockCountrySpan;
    @Mock
    private WebElement mockTeamLinkElement;
    @Mock
    private WebElement teamNameElement;
    @Mock
    private WebElement tableBody;
    @Mock
    private WebElement summaryRow;
    @Mock
    private WebDriverWait mockWait;

    private final String teamName = "barcelona";
    private final String teamCountry = "españa";
    private final int teamId = 86;
    private final int playerId = 789;

    @BeforeEach
    void setUp() {
        lenient().doReturn(mockDriver).when(teamScrapingService).setupDriverAndNavigate(anyString());
    }

    @Test
    void getPlayerNamesFromTeam_shouldReturnPlayerNames_whenTeamIsFound() {
        // Additional mocks for player extraction
        WebElement mockPlayerList = mock(WebElement.class);
        WebElement mockPlayerRow1 = mock(WebElement.class);
        WebElement mockPlayerRow2 = mock(WebElement.class);
        WebElement mockPlayerLink1 = mock(WebElement.class);
        WebElement mockPlayerLink2 = mock(WebElement.class);
        WebElement mockPlayerNameElement1 = mock(WebElement.class);
        WebElement mockPlayerNameElement2 = mock(WebElement.class);

        // Mock team search flow
        when(mockDriver.findElement(By.className("search-result"))).thenReturn(mockDivResult);
        when(mockDivResult.findElement(By.xpath("./table[1]"))).thenReturn(mockTeamsTable);
        when(mockTeamsTable.findElement(By.tagName("tbody"))).thenReturn(mockTbody);
        when(mockTbody.findElements(By.xpath("./tr[position()>1]"))).thenReturn(Collections.singletonList(mockTeamRow));
        when(mockTeamRow.findElement(By.xpath("./td[1]/a"))).thenReturn(mockLinkEquipo);
        when(mockLinkEquipo.getText()).thenReturn(teamName); // Matching team name
        when(mockTeamRow.findElement(By.xpath("./td[2]/span"))).thenReturn(mockSpanPais);
        when(mockSpanPais.getText()).thenReturn(teamCountry); // Matching country
        when(mockLinkEquipo.getAttribute("href")).thenReturn("/teams/86/barcelona");

        // Mock navigation to team page
        when(mockDriver.navigate()).thenReturn(mock(WebDriver.Navigation.class));

        // Mock player list extraction
        when(mockDriver.findElement(By.id("player-table-statistics-body"))).thenReturn(mockPlayerList);
        when(mockPlayerList.findElements(By.xpath("./*"))).thenReturn(Arrays.asList(mockPlayerRow1, mockPlayerRow2));

        // Mock individual player elements
        when(mockPlayerRow1.findElement(By.className("player-link"))).thenReturn(mockPlayerLink1);
        when(mockPlayerLink1.findElement(By.xpath("./*[2]"))).thenReturn(mockPlayerNameElement1);
        when(mockPlayerNameElement1.getText()).thenReturn("Lionel Messi");

        when(mockPlayerRow2.findElement(By.className("player-link"))).thenReturn(mockPlayerLink2);
        when(mockPlayerLink2.findElement(By.xpath("./*[2]"))).thenReturn(mockPlayerNameElement2);
        when(mockPlayerNameElement2.getText()).thenReturn("Robert Lewandowski");

        List<String> playerNames = teamScrapingService.getPlayersNamesFromTeam(teamName, teamCountry);

        assertNotNull(playerNames);
        assertEquals(2, playerNames.size());
        assertTrue(playerNames.contains("Lionel Messi"));
        assertTrue(playerNames.contains("Robert Lewandowski"));
        verify(mockDriver).quit();
    }

    @Test
    void getPlayersNamesFromTeam_shouldThrowTeamNotFoundException_whenTeamNotInSearchResults() {
        when(mockDriver.findElement(By.className("search-result"))).thenReturn(mockDivResult);
        when(mockDivResult.findElement(By.xpath("./table[1]"))).thenReturn(mockTeamsTable);
        when(mockTeamsTable.findElement(By.tagName("tbody"))).thenReturn(mockTbody);
        when(mockTbody.findElements(By.xpath("./tr[position()>1]"))).thenReturn(Collections.singletonList(mockTeamRow));
        when(mockTeamRow.findElement(By.xpath("./td[1]/a"))).thenReturn(mockLinkEquipo);
        when(mockLinkEquipo.getText()).thenReturn("Other Team"); // Different team name
        when(mockTeamRow.findElement(By.xpath("./td[2]/span"))).thenReturn(mockSpanPais);
        when(mockSpanPais.getText()).thenReturn(teamCountry);

        TeamNotFoundException exception = assertThrows(TeamNotFoundException.class, () ->
                teamScrapingService.getPlayersNamesFromTeam(teamName, teamCountry)
        );
        assertTrue(exception.getMessage().contains("Team " + teamName + " not found in country " + teamCountry));
        verify(mockDriver).quit();
    }

    @Test
    void getPlayersNamesFromTeam_shouldThrowScrappingException_onSeleniumError() {
        when(mockDriver.findElement(By.className("search-result"))).thenThrow(new NoSuchElementException("Search error"));
        assertThrows(NoSuchElementException.class, () ->
                teamScrapingService.getPlayersNamesFromTeam(teamName, teamCountry)
        );
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentPositionOnLeague_shouldReturnPosition_whenTeamIsFound() {
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenReturn(mockTableBodyStandings);
        when(mockTableBodyStandings.findElements(By.tagName("tr"))).thenReturn(Collections.singletonList(mockStandingsRow));
        when(mockStandingsRow.getAttribute("data-team-id")).thenReturn(String.valueOf(teamId));
        when(mockStandingsRow.findElement(By.xpath("./td[1]"))).thenReturn(mockFirstCell);
        when(mockFirstCell.findElement(By.tagName("span"))).thenReturn(mockPositionSpan);
        when(mockPositionSpan.getText()).thenReturn("3");

        int position = teamScrapingService.getCurrentPositionOnLeague(teamId);

        assertEquals(3, position);
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentPositionOnLeague_shouldThrowTeamNotFoundException_whenTeamNotInStandings() {
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenReturn(mockTableBodyStandings);
        when(mockTableBodyStandings.findElements(By.tagName("tr"))).thenReturn(Collections.singletonList(mockStandingsRow));
        when(mockStandingsRow.getAttribute("data-team-id")).thenReturn("999"); // Different team ID

        TeamNotFoundException exception = assertThrows(TeamNotFoundException.class, () ->
                teamScrapingService.getCurrentPositionOnLeague(teamId)
        );
        assertTrue(exception.getMessage().contains("Team ID " + teamId + " not found in league standings"));
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentPositionOnLeague_shouldThrowScrappingException_whenPositionTextIsInvalid() {
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenReturn(mockTableBodyStandings);
        when(mockTableBodyStandings.findElements(By.tagName("tr"))).thenReturn(Collections.singletonList(mockStandingsRow));
        when(mockStandingsRow.getAttribute("data-team-id")).thenReturn(String.valueOf(teamId));
        when(mockStandingsRow.findElement(By.xpath("./td[1]"))).thenReturn(mockFirstCell);
        when(mockFirstCell.findElement(By.tagName("span"))).thenReturn(mockPositionSpan);
        when(mockPositionSpan.getText()).thenReturn("NotANumber");

        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.getCurrentPositionOnLeague(teamId)
        );
        assertTrue(exception.getMessage().contains("Error parsing position text 'NotANumber'"));
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentPositionOnLeague_shouldThrowTeamNotFoundException_whenPositionTextIsEmpty() {
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenReturn(mockTableBodyStandings);
        when(mockTableBodyStandings.findElements(By.tagName("tr"))).thenReturn(Collections.singletonList(mockStandingsRow));
        when(mockStandingsRow.getAttribute("data-team-id")).thenReturn(String.valueOf(teamId));
        when(mockStandingsRow.findElement(By.xpath("./td[1]"))).thenReturn(mockFirstCell);
        when(mockFirstCell.findElement(By.tagName("span"))).thenReturn(mockPositionSpan);
        when(mockPositionSpan.getText()).thenReturn("  ");

        TeamNotFoundException exception = assertThrows(TeamNotFoundException.class, () ->
                teamScrapingService.getCurrentPositionOnLeague(teamId)
        );
        assertTrue(exception.getMessage().contains("Team ID " + teamId + " found, but has no position text"));
        verify(mockDriver).quit();
    }


    @Test
    void getCurrentPositionOnLeague_shouldThrowScrappingException_onGeneralSeleniumError() {
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenThrow(new TimeoutException("Timeout waiting for table"));

        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.getCurrentPositionOnLeague(teamId)
        );
        assertTrue(exception.getMessage().contains("Error scraping current position on league"));
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentRankingOfTeam_shouldThrowScrappingException_onSeleniumError() {
        String ratingXPath = "//tbody[@id='top-team-stats-summary-content']/tr[last()]/td[@class='rating']/strong";
        when(mockDriver.findElement(By.xpath(ratingXPath))).thenThrow(new TimeoutException("Timeout"));

        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.getCurrentRankingOfTeam(teamId)
        );
        assertTrue(exception.getMessage().contains("Error scraping current ranking of team"));
        verify(mockDriver).quit();
    }

    @Test
    void scrapTeamDataById_shouldReturnTeam_whenDataIsValid() {
        when(mockDriver.findElement(By.className("team-header-name"))).thenReturn(mockTeamNameSpan);
        when(mockTeamNameSpan.getText()).thenReturn(teamName);
        when(mockDriver.findElement(By.cssSelector("#breadcrumb-nav a"))).thenReturn(mockLeagueHref);
        when(mockLeagueHref.getText()).thenReturn("La Liga");
        when(mockDriver.findElement(By.cssSelector(".iconize.iconize-icon-left"))).thenReturn(mockCountrySpan);
        when(mockCountrySpan.getText()).thenReturn(teamCountry);

        Team team = teamScrapingService.scrapTeamDataById(teamId);

        assertNotNull(team);
        assertEquals(teamId, team.getId());
        assertEquals(teamName, team.getName());
        assertEquals(teamCountry, team.getCountry());
        assertEquals("La Liga", team.getLeague());
        verify(mockDriver).quit();
    }

    @Test
    void scrapTeamDataById_shouldThrowSeleniumException_onElementError() {
        when(mockDriver.findElement(By.className("team-header-name"))).thenThrow(new NoSuchElementException("Error finding team name"));

        assertThrows(TeamNotFoundException.class, () ->
                teamScrapingService.scrapTeamDataById(teamId)
        );
        verify(mockDriver).quit();
    }

    @Test
    void scrapActualTeamFromPlayer_shouldReturnTeamId_whenDataIsValid() {
        when(mockDriver.findElement(By.className("team-link"))).thenReturn(mockTeamLinkElement);
        when(mockTeamLinkElement.getAttribute("href")).thenReturn("/teams/" + teamId + "/some-team-name");

        int actualTeamId = teamScrapingService.scrapActualTeamFromPlayer(playerId);

        assertEquals(teamId, actualTeamId);
        verify(mockDriver).quit();
    }

    @Test
    void scrapActualTeamFromPlayer_shouldThrowScrappingException_whenHrefIsInvalidFormat() {
        when(mockDriver.findElement(By.className("team-link"))).thenReturn(mockTeamLinkElement);
        when(mockTeamLinkElement.getAttribute("href")).thenReturn("/teams/not_an_id/some-team-name");

        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.scrapActualTeamFromPlayer(playerId)
        );
        assertTrue(exception.getMessage().contains("Error scraping team data"));
        verify(mockDriver).quit();
    }

    @Test
    void scrapActualTeamFromPlayer_shouldThrowScrappingException_onSeleniumError() {
        when(mockDriver.findElement(By.className("team-link"))).thenThrow(new NoSuchElementException("Error finding team link"));
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                teamScrapingService.scrapActualTeamFromPlayer(playerId)
        );
        assertTrue(exception.getMessage().contains("Error scraping team data"));
        verify(mockDriver).quit();
    }

    @Test
    void setupDriverAndNavigate_shouldBeCalledAndQuit_evenIfScrapingFailsMidway() {
        doReturn(mockDriver).when(teamScrapingService).setupDriverAndNavigate(anyString());
        when(mockDriver.findElement(By.id("standings-15375-content"))).thenThrow(new NoSuchElementException("Table not found"));

        assertThrows(ScrappingException.class, () -> teamScrapingService.getCurrentPositionOnLeague(teamId));

        verify(teamScrapingService).setupDriverAndNavigate(anyString());
        verify(mockDriver).quit();
    }

    @Test
    void setupDriverAndNavigate_shouldNotCallQuit_ifSetupItselfFails() {
        doThrow(new RuntimeException("Failed to setup driver")).when(teamScrapingService).setupDriverAndNavigate(anyString());
        assertThrows(ScrappingException.class, () -> teamScrapingService.getCurrentPositionOnLeague(teamId));

        verify(teamScrapingService).setupDriverAndNavigate(anyString());
        verify(mockDriver, never()).quit();
    }

    @Test
    void scrapTeamStatisticsById_shouldReturnTeamStatistics_whenDataIsValid() {
        // Arrange
        int teamId = 42;

        // Mock setupDriverAndNavigate to return our mock driver
        doReturn(mockDriver).when(teamScrapingService).setupDriverAndNavigate(anyString());

        // 1. Simular WebDriverWait con respuesta en orden: first wait -> tableBody, second wait -> teamNameElement
        doReturn(mockWait).when(teamScrapingService).createWait(mockDriver);
        when(mockWait.until(any())).thenReturn(tableBody, teamNameElement);

        // 2. Simular estructura HTML esperada por el scraping
        when(mockDriver.findElement(By.id("top-team-stats-summary-content"))).thenReturn(tableBody);
        when(tableBody.findElement(By.xpath("./tr[last()]"))).thenReturn(summaryRow);

        // 3. Celdas de la fila
        WebElement cell1 = mock(WebElement.class);
        WebElement cell2 = mock(WebElement.class);
        WebElement cell3 = mock(WebElement.class);
        WebElement cell4 = mock(WebElement.class);
        WebElement cell5 = mock(WebElement.class); // Card cell
        WebElement cell6 = mock(WebElement.class);
        WebElement cell7 = mock(WebElement.class);
        WebElement cell8 = mock(WebElement.class);
        WebElement cell9 = mock(WebElement.class);

        List<WebElement> cells = Arrays.asList(cell1, cell2, cell3, cell4, cell5, cell6, cell7, cell8, cell9);
        when(summaryRow.findElements(By.tagName("td"))).thenReturn(cells);

        // 4. Mock strong elements for each cell
        WebElement strongElement1 = mock(WebElement.class);
        WebElement strongElement2 = mock(WebElement.class);
        WebElement strongElement3 = mock(WebElement.class);
        WebElement strongElement5 = mock(WebElement.class);
        WebElement strongElement6 = mock(WebElement.class);
        WebElement strongElement7 = mock(WebElement.class);
        WebElement strongElement8 = mock(WebElement.class);

        when(cell2.findElement(By.tagName("strong"))).thenReturn(strongElement1);
        when(cell3.findElement(By.tagName("strong"))).thenReturn(strongElement2);
        when(cell4.findElement(By.tagName("strong"))).thenReturn(strongElement3);
        when(cell6.findElement(By.tagName("strong"))).thenReturn(strongElement5);
        when(cell7.findElement(By.tagName("strong"))).thenReturn(strongElement6);
        when(cell8.findElement(By.tagName("strong"))).thenReturn(strongElement7);
        when(cell9.findElement(By.tagName("strong"))).thenReturn(strongElement8);

        // Mock text values for strong elements
        when(strongElement1.getText()).thenReturn("20"); // totalMatchesPlayedStr
        when(strongElement2.getText()).thenReturn("45"); // totalGoalsStr
        when(strongElement3.getText()).thenReturn("12.5"); // avgShotsPerGameStr
        when(strongElement5.getText()).thenReturn("55.2"); // avgPossessionStr
        when(strongElement6.getText()).thenReturn("82.1"); // avgPassSuccessStr
        when(strongElement7.getText()).thenReturn("8.3"); // avgAerialWonPerGameStr
        when(strongElement8.getText()).thenReturn("7.25"); // overallRatingStr

        // 5. Mock card elements in cell5 (index 4 is the card cell according to your code)
        WebElement yellowCardStrong = mock(WebElement.class);
        WebElement redCardStrong = mock(WebElement.class);

        when(cell5.findElement(By.xpath(".//span[@class='yellow-card-box']/strong"))).thenReturn(yellowCardStrong);
        when(cell5.findElement(By.xpath(".//span[@class='red-card-box']/strong"))).thenReturn(redCardStrong);
        when(yellowCardStrong.getText()).thenReturn("15"); // totalYellowCardsStr
        when(redCardStrong.getText()).thenReturn("2"); // totalRedCardsStr

        // 6. Nombre del equipo
        when(teamNameElement.getText()).thenReturn("FC Test");

        // Act
        TeamStatisticsDTO result = teamScrapingService.scrapTeamStatisticsById(teamId);

        // Assert
        assertNotNull(result);
        assertEquals("FC Test", result.getName());
        // Add more assertions based on the expected TeamStatisticsDTO structure
        verify(mockDriver).quit();
    }


    @Test
    void scrapTeamStatisticsById_shouldThrowScrappingException_whenTeamNameNotVisible() {
        doReturn(mockDriver).when(teamScrapingService).setupDriverAndNavigate(anyString());
        doReturn(mockWait).when(teamScrapingService).createWait(mockDriver);

        // Simular timeout en visibilidad
        when(mockWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span.team-header-name"))))
                .thenThrow(new TimeoutException("Not visible"));

        ScrappingException ex = assertThrows(ScrappingException.class, () ->
                teamScrapingService.scrapTeamStatisticsById(teamId));

        assertTrue(ex.getMessage().contains("Error scraping team statistics"));
    }


}
