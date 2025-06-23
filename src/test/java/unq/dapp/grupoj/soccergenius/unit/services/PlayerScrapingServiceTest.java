package unq.dapp.grupoj.soccergenius.unit.services;

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
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.model.player.summary.CurrentParticipationsSummary;
import unq.dapp.grupoj.soccergenius.model.player.summary.HistoricalParticipationsSummary;
import unq.dapp.grupoj.soccergenius.services.external.whoscored.PlayerScrapingService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("unit")
@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class PlayerScrapingServiceTest {
    @Mock
    private WebDriver mockDriver;

    @Spy
    private PlayerScrapingService playerScrapingService = new PlayerScrapingService();

    // Mocks para scrapPlayerData
    @Mock
    private WebElement mockNameContainerElement;
    @Mock
    private WebElement mockAgeElement;
    @Mock
    private WebElement mockNationalityElement;
    @Mock
    private WebElement mockCountryElement;
    @Mock
    private WebElement mockPositionsDivElement;
    @Mock
    private WebElement mockHeightElement;

    // Mocks para extractRatingFromPage
    @Mock
    private WebElement mockGridPresenceElement; // Para satisfacer WebDriverWait
    @Mock
    private WebElement mockTotalRowElement;
    @Mock
    private WebElement mockRatingCellElement;

    private final int playerId = 123;
    private final String playerName = "Test Player";
    private final int playerAge = 30;
    private final String playerNationality = "Testlandia";
    private final String playerHeight = "180cm";
    private final List<String> playerPositions = Arrays.asList("Forward", "Midfielder");
    private final double playerRating = 8.5;

    @BeforeEach
    void setUp() {
        // Mock común para setupDriverAndNavigate
        // lenient() se usa porque no todas las pruebas llamarán a setupDriverAndNavigate directamente,
        // o algunas podrían necesitar que lance una excepción.
        lenient().doReturn(mockDriver).when(playerScrapingService).setupDriverAndNavigate(anyString());
    }

    // Pruebas para scrapPlayerData
    @Test
    void scrapPlayerData_shouldReturnPlayerData_whenPageIsValid() {
        // Arrange
        when(mockDriver.findElements(By.xpath("//*[contains(text(), 'The page you requested does not exist')]")))
                .thenReturn(Collections.emptyList()); // La página existe

        when(mockDriver.findElement(By.xpath("//span[contains(text(),'Nombre: ')]/parent::div"))).thenReturn(mockNameContainerElement);
        when(mockNameContainerElement.getText()).thenReturn("Nombre: " + playerName + " ");

        when(mockDriver.findElement(By.xpath("//span[contains(text(),'Edad: ')]/parent::div"))).thenReturn(mockAgeElement);
        when(mockAgeElement.getText()).thenReturn("Edad: " + playerAge + " aos");

        when(mockDriver.findElement(By.xpath("//span[contains(text(),'Nacionalidad:')]/parent::div"))).thenReturn(mockNationalityElement);
        when(mockNationalityElement.findElement(By.className("iconize-icon-left"))).thenReturn(mockCountryElement);
        when(mockCountryElement.getText()).thenReturn(playerNationality + " FlagText");

        when(mockDriver.findElement(By.xpath("//span[contains(text(),'Posiciones: ')]/parent::div"))).thenReturn(mockPositionsDivElement);
        WebElement mockPositionSpan1 = mock(WebElement.class);
        WebElement mockPositionSpan2 = mock(WebElement.class);
        when(mockPositionSpan1.getText()).thenReturn(playerPositions.get(0) + " ");
        when(mockPositionSpan2.getText()).thenReturn(" " + playerPositions.get(1));
        when(mockPositionsDivElement.findElements(By.xpath(".//span[@style='display: inline-block;']")))
                .thenReturn(Arrays.asList(mockPositionSpan1, mockPositionSpan2));

        when(mockDriver.findElement(By.xpath("//span[contains(text(),'Altura:')]/parent::div"))).thenReturn(mockHeightElement);
        when(mockHeightElement.getText()).thenReturn("Altura:" + playerHeight + " ");

        // Act
        Player result = playerScrapingService.scrapPlayerData(playerId);

        // Assert
        assertNotNull(result);
        assertEquals(playerId, result.getId());
        assertEquals(playerName, result.getName());
        assertEquals(playerAge, result.getAge());
        assertEquals(playerNationality, result.getNationality());
        assertEquals(playerHeight, result.getHeight());
        assertEquals(playerPositions.size(), result.getPositions().size());
        assertTrue(result.getPositions().containsAll(playerPositions));

        verify(mockDriver).quit();
    }

    @Test
    void scrapPlayerData_shouldReturnNull_whenPlayerPageDoesNotExist() {
        // Arrange
        WebElement mockErrorMessage = mock(WebElement.class);
        when(mockDriver.findElements(By.xpath("//*[contains(text(), 'The page you requested does not exist')]")))
                .thenReturn(Collections.singletonList(mockErrorMessage)); // La página NO existe

        // Act
        Player result = playerScrapingService.scrapPlayerData(playerId);

        // Assert
        assertNull(result);
        verify(mockDriver).quit();
    }

    @Test
    void scrapPlayerData_shouldThrowScrappingException_whenSeleniumErrorOccursDuringSetup() {
        // Arrange
        doThrow(new RuntimeException("Selenium connection failed"))
                .when(playerScrapingService).setupDriverAndNavigate(anyString());

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                playerScrapingService.scrapPlayerData(playerId)
        );
        assertTrue(exception.getMessage().contains("Error scraping player data"));
        verify(mockDriver, never()).quit(); // El driver no se inicializó con éxito
    }

    @Test
    void scrapPlayerData_shouldThrowScrappingException_whenElementNotFoundErrorOccurs() {
        // Arrange
        when(mockDriver.findElements(By.xpath("//*[contains(text(), 'The page you requested does not exist')]")))
                .thenReturn(Collections.emptyList()); // La página existe
        when(mockDriver.findElement(By.xpath("//span[contains(text(),'Nombre: ')]/parent::div")))
                .thenThrow(new NoSuchElementException("Cannot find name element"));

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                playerScrapingService.scrapPlayerData(playerId)
        );
        assertTrue(exception.getMessage().contains("Error scraping player data"));
        verify(mockDriver).quit(); // El driver se inicializó, así que quit() debería llamarse
    }

    // Pruebas para getCurrentParticipationInfo
    @Test
    void getCurrentParticipationInfo_shouldReturnSummary_whenDataIsValid() {
        // Arrange
        Player testPlayer = new Player(playerId, playerName, playerAge, playerNationality, playerHeight, playerPositions);

        // Mock para WebDriverWait y extractRatingFromPage
        when(mockDriver.findElement(By.id("top-player-stats-summary-grid"))).thenReturn(mockGridPresenceElement); // Para WebDriverWait
        when(mockDriver.findElement(By.xpath("//table[@id='top-player-stats-summary-grid']/tbody/tr[last()]"))).thenReturn(mockTotalRowElement);
        when(mockTotalRowElement.findElement(By.xpath("./td[@class='rating']/strong"))).thenReturn(mockRatingCellElement);
        when(mockRatingCellElement.getText()).thenReturn(String.valueOf(playerRating));

        // Act
        CurrentParticipationsSummary summary = playerScrapingService.getCurrentParticipationInfo(testPlayer);

        // Assert
        assertNotNull(summary);
        assertEquals(testPlayer, summary.getPlayer());
        assertEquals(playerRating, summary.getRating());
        verify(mockDriver).quit();
    }

    @Test
    void getCurrentParticipationInfo_shouldThrowScrappingException_whenSeleniumErrorOccursDuringSetup() {
        // Arrange
        Player testPlayer = new Player(playerId, playerName, playerAge, playerNationality, playerHeight, playerPositions);
        doThrow(new RuntimeException("Selenium connection failed"))
                .when(playerScrapingService).setupDriverAndNavigate(anyString());

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                playerScrapingService.getCurrentParticipationInfo(testPlayer)
        );
        assertTrue(exception.getMessage().contains("Error scraping player participation info"));
        verify(mockDriver, never()).quit();
    }

    @Test
    void getCurrentParticipationInfo_shouldThrowScrappingException_whenRatingExtractionFails() {
        // Arrange
        Player testPlayer = new Player(playerId, playerName, playerAge, playerNationality, playerHeight, playerPositions);
        when(mockDriver.findElement(By.id("top-player-stats-summary-grid"))).thenReturn(mockGridPresenceElement); // Para WebDriverWait
        when(mockDriver.findElement(By.xpath("//table[@id='top-player-stats-summary-grid']/tbody/tr[last()]")))
                .thenThrow(new NoSuchElementException("Cannot find total row"));

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                playerScrapingService.getCurrentParticipationInfo(testPlayer)
        );
        assertTrue(exception.getMessage().contains("Error scraping player participation info"));
        verify(mockDriver).quit();
    }

    // Pruebas para getHistoryInfo
    @Test
    void getHistoryInfo_shouldReturnSummary_whenDataIsValid() {
        // Arrange
        Player testPlayer = new Player(playerId, playerName, playerAge, playerNationality, playerHeight, playerPositions);

        // Mock para WebDriverWait y extractRatingFromPage
        when(mockDriver.findElement(By.id("top-player-stats-summary-grid"))).thenReturn(mockGridPresenceElement); // Para WebDriverWait
        when(mockDriver.findElement(By.xpath("//table[@id='top-player-stats-summary-grid']/tbody/tr[last()]"))).thenReturn(mockTotalRowElement);
        when(mockTotalRowElement.findElement(By.xpath("./td[@class='rating']/strong"))).thenReturn(mockRatingCellElement);
        when(mockRatingCellElement.getText()).thenReturn(String.valueOf(playerRating));

        // Act
        HistoricalParticipationsSummary summary = playerScrapingService.getHistoryInfo(testPlayer);

        // Assert
        assertNotNull(summary);
        assertEquals(testPlayer, summary.getPlayer());
        assertEquals(playerRating, summary.getRating());
        verify(mockDriver).quit();
    }

    @Test
    void getHistoryInfo_shouldThrowScrappingException_whenSeleniumErrorOccursDuringSetup() {
        // Arrange
        Player testPlayer = new Player(playerId, playerName, playerAge, playerNationality, playerHeight, playerPositions);
        doThrow(new RuntimeException("Selenium connection failed"))
                .when(playerScrapingService).setupDriverAndNavigate(anyString());

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                playerScrapingService.getHistoryInfo(testPlayer)
        );
        assertTrue(exception.getMessage().contains("Error scraping player history info"));
        verify(mockDriver, never()).quit();
    }

    @Test
    void getHistoryInfo_shouldThrowScrappingException_whenRatingExtractionFails() {
        // Arrange
        Player testPlayer = new Player(playerId, playerName, playerAge, playerNationality, playerHeight, playerPositions);
        when(mockDriver.findElement(By.id("top-player-stats-summary-grid"))).thenReturn(mockGridPresenceElement); // Para WebDriverWait
        when(mockDriver.findElement(By.xpath("//table[@id='top-player-stats-summary-grid']/tbody/tr[last()]")))
                .thenThrow(new NoSuchElementException("Cannot find total row for history"));

        // Act & Assert
        ScrappingException exception = assertThrows(ScrappingException.class, () ->
                playerScrapingService.getHistoryInfo(testPlayer)
        );
        assertTrue(exception.getMessage().contains("Error scraping player history info"));
        verify(mockDriver).quit();
    }
}
