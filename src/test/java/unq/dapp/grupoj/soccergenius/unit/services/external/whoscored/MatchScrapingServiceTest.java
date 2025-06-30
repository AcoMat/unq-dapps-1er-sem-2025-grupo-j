package unq.dapp.grupoj.soccergenius.unit.services.external.whoscored;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.test.context.ActiveProfiles;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
import unq.dapp.grupoj.soccergenius.model.Match;
import unq.dapp.grupoj.soccergenius.services.external.whoscored.MatchScrapingService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("unit")
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MatchScrapingServiceTest {
    @Mock
    private WebDriver mockDriver;

    @Mock
    private WebElement mockMatchElement;
    @Mock
    private WebElement mockTeamNameElement1;
    @Mock
    private WebElement mockTeamNameElement2;
    @Mock
    private WebElement mockScoreLinkElement;
    @Mock
    private WebElement mockGridElement;
    @Mock
    private WebElement mockRowElement;

    // We spy on the actual service to test its methods while being able to mock
    // calls to setupDriverAndNavigate and its own findMatchLink (for getPreviousMeetings tests).
    @Spy
    private MatchScrapingService matchScrapingService = new MatchScrapingService();

    private final String teamA = "Team A";
    private final String teamB = "Team B";
    private final String expectedMatchUrl = "http://example.com/match/123";

    @BeforeEach
    void setUp() {
        // Common mock setup for setupDriverAndNavigate
        // lenient() is used because not all tests will call setupDriverAndNavigate
        lenient().doReturn(mockDriver).when(matchScrapingService).setupDriverAndNavigate(anyString());
    }

    // Tests for findMatchLink
    @Test
    void findMatchLink_shouldReturnMatchUrl_whenMatchIsFound() {
        // Arrange
        when(mockDriver.findElements(By.className("Match-module_match__XlKTY")))
                .thenReturn(Collections.singletonList(mockMatchElement));
        when(mockMatchElement.findElements(By.className("Match-module_teamNameText__Dqv-G")))
                .thenReturn(Arrays.asList(mockTeamNameElement1, mockTeamNameElement2));
        when(mockTeamNameElement1.getText()).thenReturn(teamA);
        when(mockTeamNameElement2.getText()).thenReturn(teamB);
        when(mockMatchElement.findElement(By.className("Match-module_score__5Ghhj")))
                .thenReturn(mockScoreLinkElement);
        when(mockScoreLinkElement.getAttribute("href")).thenReturn(expectedMatchUrl);

        // Act
        String matchUrl = matchScrapingService.findMatchLink(teamA, teamB);

        // Assert
        assertEquals(expectedMatchUrl, matchUrl);
        verify(mockDriver).quit();
    }

    @Test
    void findMatchLink_shouldReturnMatchUrl_whenMatchIsFoundOrderReversed() {
        // Arrange
        when(mockDriver.findElements(By.className("Match-module_match__XlKTY")))
                .thenReturn(Collections.singletonList(mockMatchElement));
        when(mockMatchElement.findElements(By.className("Match-module_teamNameText__Dqv-G")))
                .thenReturn(Arrays.asList(mockTeamNameElement1, mockTeamNameElement2));
        when(mockTeamNameElement1.getText()).thenReturn(teamB); // Reversed order
        when(mockTeamNameElement2.getText()).thenReturn(teamA);
        when(mockMatchElement.findElement(By.className("Match-module_score__5Ghhj")))
                .thenReturn(mockScoreLinkElement);
        when(mockScoreLinkElement.getAttribute("href")).thenReturn(expectedMatchUrl);

        // Act
        String matchUrl = matchScrapingService.findMatchLink(teamA, teamB);

        // Assert
        assertEquals(expectedMatchUrl, matchUrl);
        verify(mockDriver).quit();
    }

    @Test
    void findMatchLink_shouldReturnNull_whenMatchIsNotFound() {
        // Arrange
        when(mockDriver.findElements(By.className("Match-module_match__XlKTY")))
                .thenReturn(Collections.singletonList(mockMatchElement));
        when(mockMatchElement.findElements(By.className("Match-module_teamNameText__Dqv-G")))
                .thenReturn(Arrays.asList(mockTeamNameElement1, mockTeamNameElement2));
        when(mockTeamNameElement1.getText()).thenReturn("Other Team 1");
        when(mockTeamNameElement2.getText()).thenReturn("Other Team 2");

        // Act
        String matchUrl = matchScrapingService.findMatchLink(teamA, teamB);

        // Assert
        assertNull(matchUrl);
        verify(mockDriver).quit();
    }

    @Test
    void findMatchLink_shouldReturnNull_whenNoMatchesOnPage() {
        // Arrange
        when(mockDriver.findElements(By.className("Match-module_match__XlKTY")))
                .thenReturn(Collections.emptyList());

        // Act
        String matchUrl = matchScrapingService.findMatchLink(teamA, teamB);

        // Assert
        assertNull(matchUrl);
        verify(mockDriver).quit();
    }

    @Test
    void findMatchLink_shouldThrowScrappingException_whenSeleniumErrorOccurs() {
        // Arrange
        doThrow(new RuntimeException("Selenium connection failed"))
                .when(matchScrapingService).setupDriverAndNavigate(anyString());

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                matchScrapingService.findMatchLink(teamA, teamB)
        );
        assertTrue(exception.getMessage().contains("Error scraping match links"));
        verify(mockDriver, never()).quit(); // Driver was not successfully initialized
    }

    @Test
    void findMatchLink_shouldThrowScrappingException_whenElementFindingErrorOccurs() {
        // Arrange
        when(mockDriver.findElements(By.className("Match-module_match__XlKTY")))
                .thenThrow(new RuntimeException("Cannot find elements"));

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                matchScrapingService.findMatchLink(teamA, teamB)
        );
        assertTrue(exception.getMessage().contains("Error scraping match links"));
        verify(mockDriver).quit(); // Driver was initialized
    }


    // Tests for getPreviousMeetings
    @Test
    void getPreviousMeetings_shouldReturnEmptyList_whenMatchLinkIsNotFound() {
        // Arrange
        doReturn(null).when(matchScrapingService).findMatchLink(teamA, teamB);

        // Act
        List<Match> previousMeetings = matchScrapingService.getPreviousMeetings(teamA, teamB);

        // Assert
        assertTrue(previousMeetings.isEmpty());
        // Ensure setupDriverAndNavigate for previous meetings page is not called
        verify(matchScrapingService, times(0)).setupDriverAndNavigate(expectedMatchUrl);
    }

    @Test
    void getPreviousMeetings_shouldReturnListOfMatches_whenDataIsScrapedSuccessfully() {
        // Arrange
        doReturn(expectedMatchUrl).when(matchScrapingService).findMatchLink(teamA, teamB);
        // setupDriverAndNavigate is already mocked in @BeforeEach to return mockDriver for expectedMatchUrl

        when(mockDriver.findElement(By.id("previous-meetings-grid"))).thenReturn(mockGridElement);
        when(mockGridElement.findElements(By.className("divtable-row"))).thenReturn(Collections.singletonList(mockRowElement));

        // Mock data for one row
        when(mockRowElement.getAttribute("data-id")).thenReturn("match123");

        // Mock extractDate
        WebElement mockDateDiv = mock(WebElement.class);
        when(mockRowElement.findElement(By.cssSelector(".date-long > div"))).thenReturn(mockDateDiv);
        when(mockDateDiv.getText()).thenReturn("01 Jan 2023");

        // Mock extractTeams
        WebElement mockHomeTeamEl = mock(WebElement.class);
        WebElement mockAwayTeamEl = mock(WebElement.class);
        when(mockRowElement.findElement(By.cssSelector(".horizontal-match-display.team.home .team-link"))).thenReturn(mockHomeTeamEl);
        when(mockHomeTeamEl.getText()).thenReturn(teamA);
        when(mockRowElement.findElement(By.cssSelector(".horizontal-match-display.team.away .team-link"))).thenReturn(mockAwayTeamEl);
        when(mockAwayTeamEl.getText()).thenReturn(teamB);

        // Mock extractScores
        WebElement mockResultEl = mock(WebElement.class);
        when(mockRowElement.findElement(By.cssSelector(".result > a.horiz-match-link"))).thenReturn(mockResultEl);
        when(mockResultEl.getText()).thenReturn("2 : 1");

        // Mock extractWinner (home team wins by score, selectors fail)
        when(mockRowElement.findElement(By.cssSelector(".horizontal-match-display.team.home.winner .team-link"))).thenThrow(NoSuchElementException.class);
        when(mockRowElement.findElement(By.cssSelector(".horizontal-match-display.team.away.winner .team-link"))).thenThrow(NoSuchElementException.class);
        when(mockRowElement.findElement(By.cssSelector(".stacked-teams-display .team.winner .team-link"))).thenThrow(NoSuchElementException.class);

        // Act
        List<Match> previousMeetings = matchScrapingService.getPreviousMeetings(teamA, teamB);

        // Assert
        assertEquals(1, previousMeetings.size());
        Match match = previousMeetings.getFirst();
        assertEquals("match123", match.getId());
        assertEquals("01 Jan 2023", match.getDate());
        assertEquals(teamA, match.getHomeTeam());
        assertEquals(teamB, match.getAwayTeam());
        assertEquals("2", match.getHomeScore());
        assertEquals("1", match.getAwayScore());
        assertEquals(teamA, match.getWinner()); // Winner determined by score comparison

        verify(mockDriver).quit();
    }

    @Test
    void getPreviousMeetings_shouldCorrectlyExtractStackedDate() {
        doReturn(expectedMatchUrl).when(matchScrapingService).findMatchLink(teamA, teamB);
        when(mockDriver.findElement(By.id("previous-meetings-grid"))).thenReturn(mockGridElement);
        when(mockGridElement.findElements(By.className("divtable-row"))).thenReturn(Collections.singletonList(mockRowElement));
        when(mockRowElement.getAttribute("data-id")).thenReturn("matchStackedDate");

        // Mock extractDate - stacked
        when(mockRowElement.findElement(By.cssSelector(".date-long > div"))).thenThrow(NoSuchElementException.class);
        WebElement datePart1 = mock(WebElement.class);
        WebElement datePart2 = mock(WebElement.class);
        when(datePart1.getText()).thenReturn("15");
        when(datePart2.getText()).thenReturn("Feb 2024");
        when(mockRowElement.findElements(By.cssSelector(".date-stacked > div"))).thenReturn(Arrays.asList(datePart1, datePart2));

        // Minimal mocks for other extractors to avoid NPEs, returning empty/default values
        setupMinimalRowMocksForNonDateExtraction(mockRowElement);


        List<Match> previousMeetings = matchScrapingService.getPreviousMeetings(teamA, teamB);
        assertEquals("15 Feb 2024", previousMeetings.getFirst().getDate());
        verify(mockDriver).quit();
    }

    @Test
    void getPreviousMeetings_shouldThrowScrappingException_whenSeleniumErrorOccurs() {
        // Arrange
        doReturn(expectedMatchUrl).when(matchScrapingService).findMatchLink(teamA, teamB);
        doThrow(new RuntimeException("Selenium connection failed during previous meetings"))
                .when(matchScrapingService).setupDriverAndNavigate(expectedMatchUrl);

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                matchScrapingService.getPreviousMeetings(teamA, teamB)
        );
        assertTrue(exception.getMessage().contains("Error scraping previous meetings"));
        verify(mockDriver, never()).quit(); // Driver not set up for this part
    }

    @Test
    void getPreviousMeetings_shouldThrowScrappingException_whenElementFindingErrorOccurs() {
        // Arrange
        doReturn(expectedMatchUrl).when(matchScrapingService).findMatchLink(teamA, teamB);
        // setupDriverAndNavigate is mocked to return mockDriver

        when(mockDriver.findElement(By.id("previous-meetings-grid")))
                .thenThrow(new RuntimeException("Cannot find grid"));

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                matchScrapingService.getPreviousMeetings(teamA, teamB)
        );
        assertTrue(exception.getMessage().contains("Error scraping previous meetings"));
        verify(mockDriver).quit(); // Driver was initialized
    }

    // Helper to setup minimal mocks for a row when focusing on a specific extractor
    private void setupMinimalRowMocksForNonDateExtraction(WebElement rowElement) {
        // Teams
        WebElement mockHomeTeamEl = mock(WebElement.class);
        WebElement mockAwayTeamEl = mock(WebElement.class);
        lenient().when(rowElement.findElement(By.cssSelector(".horizontal-match-display.team.home .team-link"))).thenReturn(mockHomeTeamEl);
        lenient().when(mockHomeTeamEl.getText()).thenReturn(teamA);
        lenient().when(rowElement.findElement(By.cssSelector(".horizontal-match-display.team.away .team-link"))).thenReturn(mockAwayTeamEl);
        lenient().when(mockAwayTeamEl.getText()).thenReturn(teamB);
        // Scores (default to empty or non-impacting values)
        WebElement mockResultEl = mock(WebElement.class);
        lenient().when(rowElement.findElement(By.cssSelector(".result > a.horiz-match-link"))).thenReturn(mockResultEl);
        lenient().when(mockResultEl.getText()).thenReturn("0 : 0"); // Default scores
        // Winner (default to no CSS winner)
        lenient().when(rowElement.findElement(By.cssSelector(".horizontal-match-display.team.home.winner .team-link"))).thenThrow(NoSuchElementException.class);
        lenient().when(rowElement.findElement(By.cssSelector(".horizontal-match-display.team.away.winner .team-link"))).thenThrow(NoSuchElementException.class);
        lenient().when(rowElement.findElement(By.cssSelector(".stacked-teams-display .team.winner .team-link"))).thenThrow(NoSuchElementException.class);
    }
}
