package unq.dapp.grupoj.soccergenius.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.model.player.summary.CurrentParticipationsSummary;
import unq.dapp.grupoj.soccergenius.model.player.summary.HistoricalParticipationsSummary;
import unq.dapp.grupoj.soccergenius.repository.PlayerRepository;
import unq.dapp.grupoj.soccergenius.services.player.PlayerService;
import unq.dapp.grupoj.soccergenius.services.scrapping.WebScrapingService;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {
    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private WebScrapingService webScrapingService;

    @InjectMocks
    private PlayerService playerService; // Asumiendo que PlayerService ya está modificado para inyectar WebScrapingService

    private Player mockPlayer;
    private final int playerId = 1;


    @BeforeEach
    void setUp() {
        List<String> messiPositions = new ArrayList<>();
        messiPositions.add("Forward");
        messiPositions.add("Attacking Midfielder");

        mockPlayer = new Player(playerId, "Lionel Messi", 36, "Argentina", "170cm", messiPositions);
    }

    @Test
    void testGetPlayer_WhenPlayerExistsInRepository_ReturnsPlayer() {
        // Arrange
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(mockPlayer));

        // Act
        Player foundPlayer = playerService.getPlayer(playerId);

        // Assert
        assertNotNull(foundPlayer);
        assertEquals(playerId, foundPlayer.getId());
        assertEquals("Lionel Messi", foundPlayer.getName());
        List<String> positionsList = new ArrayList<>();
        positionsList.add("Forward");
        positionsList.add("Attacking Midfielder");
        assertEquals(positionsList, foundPlayer.getPositions());
        verify(playerRepository, times(1)).findById(playerId);
        verify(webScrapingService, never()).scrapPlayerData(anyInt());
        verify(webScrapingService, never()).getCurrentParticipationInfo(any(Player.class));
        verify(webScrapingService, never()).getHistoryInfo(any(Player.class));
        verify(playerRepository, never()).save(any(Player.class));
    }


    @Test
    void testGetPlayer_WhenPlayerNotInRepository_ScrapesAndReturnsPlayer() {
        // Arrange
        int newPlayerId = 2;
        List<String> ronaldoPositions = new ArrayList<>();
        ronaldoPositions.add("Forward");
        ronaldoPositions.add("Striker");

        // Player creado por el scraping
        Player scrapedPlayer = new Player(newPlayerId, "Cristiano Ronaldo", 38, "Portugal", "187cm", ronaldoPositions);

        CurrentParticipationsSummary mockCurrentSummary = mock(CurrentParticipationsSummary.class);
        HistoricalParticipationsSummary mockHistoricalSummary = mock(HistoricalParticipationsSummary.class);

        when(playerRepository.findById(newPlayerId)).thenReturn(Optional.empty());
        when(webScrapingService.scrapPlayerData(newPlayerId)).thenReturn(scrapedPlayer);
        when(webScrapingService.getCurrentParticipationInfo(scrapedPlayer)).thenReturn(mockCurrentSummary);
        when(webScrapingService.getHistoryInfo(scrapedPlayer)).thenReturn(mockHistoricalSummary);

        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Simula que save devuelve el jugador guardado

        // Act
        Player foundPlayer = playerService.getPlayer(newPlayerId);

        // Assert
        assertNotNull(foundPlayer);
        assertEquals(newPlayerId, foundPlayer.getId());
        assertEquals("Cristiano Ronaldo", foundPlayer.getName());
        assertEquals(ronaldoPositions, foundPlayer.getPositions());
        assertEquals(mockCurrentSummary, foundPlayer.getCurrentParticipationsSummary()); // Verificar que el summary se asignó
        assertEquals(mockHistoricalSummary, foundPlayer.getHistory()); // Verificar que el historial se asignó

        verify(playerRepository, times(1)).findById(newPlayerId);
        verify(webScrapingService, times(1)).scrapPlayerData(newPlayerId);
        verify(webScrapingService, times(1)).getCurrentParticipationInfo(scrapedPlayer);
        verify(webScrapingService, times(1)).getHistoryInfo(scrapedPlayer);
        verify(playerRepository, times(1)).save(scrapedPlayer);
    }


    @Test
    void testGetPlayer_WhenPlayerNotInRepositoryAndScrapingFails_ReturnsNull() {
        // Arrange
        int nonExistentPlayerId = 3;
        when(playerRepository.findById(nonExistentPlayerId)).thenReturn(Optional.empty());
        when(webScrapingService.scrapPlayerData(nonExistentPlayerId)).thenReturn(null); // Simula que el scraping falla

        // Act
        Player foundPlayer = playerService.getPlayer(nonExistentPlayerId);

        // Assert
        assertNull(foundPlayer);
        verify(playerRepository, times(1)).findById(nonExistentPlayerId);
        verify(webScrapingService, times(1)).scrapPlayerData(nonExistentPlayerId);
        // Verificamos que no se intentobtener más info ni guardar si el scraping inicial falló
        verify(webScrapingService, never()).getCurrentParticipationInfo(any(Player.class));
        verify(webScrapingService, never()).getHistoryInfo(any(Player.class));
        verify(playerRepository, never()).save(any(Player.class));
    }
}
