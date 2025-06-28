package unq.dapp.grupoj.soccergenius.unit.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.model.player.summary.CurrentParticipationsSummary;
import unq.dapp.grupoj.soccergenius.model.player.summary.HistoricalParticipationsSummary;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("unit")
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class PlayerTest {
    private Player player;
    private final int playerId = 1;
    private final String playerName = "Lionel Messi";
    private final int initialAge = 36;
    private final String nationality = "Argentinian";
    private final String height = "170cm";
    private final List<String> positions = Arrays.asList("Forward", "Attacking Midfielder");

    @Mock
    private Team mockTeam;

    @Mock
    private CurrentParticipationsSummary mockCurrentSummary;

    @Mock
    private HistoricalParticipationsSummary mockHistoricalSummary;

    @BeforeEach
    void setUp() {
        player = new Player(playerId, playerName, initialAge, nationality, height, positions);
    }

    @Test
    void constructor_shouldInitializeFieldsCorrectly() {
        assertNotNull(player);
        assertEquals(playerId, player.getId());
        assertEquals(playerName, player.getName());
        assertEquals(initialAge, player.getAge());
        assertEquals(nationality, player.getNationality());
        assertEquals(height, player.getHeight());
        assertEquals(positions, player.getPositions());
        assertNull(player.getActualTeam());
        assertNull(player.getCurrentParticipationsSummary());
        assertNull(player.getHistory());
        assertNotNull(player.getLastUpdate());
    }

    @Test
    void noArgsConstructor_shouldCreatePlayerInstance() {
        Player emptyPlayer = new Player();
        assertNotNull(emptyPlayer);
        // Default values for primitives will be 0 or null for objects
        assertEquals(0, emptyPlayer.getId());
        assertNull(emptyPlayer.getName());
    }

    @Test
    void setAge_shouldUpdateAgeAndLastUpdate() throws InterruptedException {
        ZonedDateTime initialUpdateTime = player.getLastUpdate();
        int newAge = 37;

        // Introduce a small delay to ensure ZonedDateTime.now() will be different
        Thread.sleep(2);

        player.setAge(newAge);

        assertEquals(newAge, player.getAge());
        assertNotNull(player.getLastUpdate());
        assertTrue(player.getLastUpdate().isAfter(initialUpdateTime));
    }

    @Test
    void updateActualTeam_shouldUpdateTeamAndLastUpdate() throws InterruptedException {
        ZonedDateTime initialUpdateTime = player.getLastUpdate();
        Thread.sleep(2); // Ensure time difference

        player.updateActualTeam(mockTeam);

        assertEquals(mockTeam, player.getActualTeam());
        assertNotNull(player.getLastUpdate());
        assertTrue(player.getLastUpdate().isAfter(initialUpdateTime));
    }

    @Test
    void setCurrentParticipationsSummary_shouldUpdateSummaryAndLastUpdate() throws InterruptedException {
        ZonedDateTime initialUpdateTime = player.getLastUpdate();
        Thread.sleep(2); // Ensure time difference

        player.setCurrentParticipationsSummary(mockCurrentSummary);

        assertEquals(mockCurrentSummary, player.getCurrentParticipationsSummary());
        assertNotNull(player.getLastUpdate());
        assertTrue(player.getLastUpdate().isAfter(initialUpdateTime));
    }

    @Test
    void setHistory_shouldUpdateHistoryAndLastUpdate() throws InterruptedException {
        ZonedDateTime initialUpdateTime = player.getLastUpdate();
        Thread.sleep(2); // Ensure time difference

        player.setHistory(mockHistoricalSummary);

        assertEquals(mockHistoricalSummary, player.getHistory());
        assertNotNull(player.getLastUpdate());
        assertTrue(player.getLastUpdate().isAfter(initialUpdateTime));
    }

    @Test
    void getPerformanceAndTendency_shouldReturnCorrectString() {
        // Arrange
        double currentRating = 8.5;
        double historicalRating = 7.8;
        double expectedTendency = currentRating - historicalRating;

        when(mockCurrentSummary.getRating()).thenReturn(currentRating);
        when(mockHistoricalSummary.getRating()).thenReturn(historicalRating);

        player.setCurrentParticipationsSummary(mockCurrentSummary);
        player.setHistory(mockHistoricalSummary);

        // Act
        String performanceAndTendency = player.getPerformanceAndTendency();

        // Assert
        String expectedString = playerName + " has a current performance index of " + currentRating +
                " and a tendency of " + expectedTendency + " compared to the historical average.";
        assertEquals(expectedString, performanceAndTendency);
    }

    @Test
    void getPerformanceAndTendency_shouldHandleZeroRatings() {
        // Arrange
        double currentRating = 0.0;
        double historicalRating = 0.0;
        double expectedTendency = 0.0;

        when(mockCurrentSummary.getRating()).thenReturn(currentRating);
        when(mockHistoricalSummary.getRating()).thenReturn(historicalRating);

        player.setCurrentParticipationsSummary(mockCurrentSummary);
        player.setHistory(mockHistoricalSummary);

        // Act
        String performanceAndTendency = player.getPerformanceAndTendency();

        // Assert
        String expectedString = playerName + " has a current performance index of " + currentRating +
                " and a tendency of " + expectedTendency + " compared to the historical average.";
        assertEquals(expectedString, performanceAndTendency);
    }

    @Test
    void getPerformanceAndTendency_shouldHandleNegativeTendency() {
        // Arrange
        double currentRating = 7.0;
        double historicalRating = 7.5;
        double expectedTendency = currentRating - historicalRating; // Will be -0.5

        when(mockCurrentSummary.getRating()).thenReturn(currentRating);
        when(mockHistoricalSummary.getRating()).thenReturn(historicalRating);

        player.setCurrentParticipationsSummary(mockCurrentSummary);
        player.setHistory(mockHistoricalSummary);

        // Act
        String performanceAndTendency = player.getPerformanceAndTendency();

        // Assert
        String expectedString = playerName + " has a current performance index of " + currentRating +
                " and a tendency of " + expectedTendency + " compared to the historical average.";
        assertEquals(expectedString, performanceAndTendency);
    }

    @Test
    void getPerformanceAndTendency_throwsNullPointerException_whenSummariesAreNull() {
        // Player is initialized with null summaries in setUp()
        assertThrows(NullPointerException.class, () -> player.getPerformanceAndTendency());
    }

}
