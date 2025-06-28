package unq.dapp.grupoj.soccergenius.unit.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import unq.dapp.grupoj.soccergenius.model.dtos.external.football_data.*;
import unq.dapp.grupoj.soccergenius.services.matches.MatchService;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("unit")
@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {

    @InjectMocks
    private MatchService matchService;

    @Test
    void testConvertMatchesToString_withNullList_returnsNoMatchesMessage() throws Exception {
        // When
        String result = invokeConvertMatchesToString(null);
        // Then
        assertEquals("No hay partidos registrados", result);
    }

    @Test
    void testConvertMatchesToString_withEmptyList_returnsNoMatchesMessage() throws Exception {
        // Given
        List<FootballDataMatchDto> matches = Collections.emptyList();

        // When
        String result = invokeConvertMatchesToString(matches);

        // Then
        assertEquals("No hay partidos registrados", result);
    }

    @Test
    void testConvertMatchesToString_withMatchesWithFullTimeScore_returnsFormattedString() throws Exception {
        // Given
        FootballDataTeamDto homeTeam = new FootballDataTeamDto(1, "Real Madrid", "RMA", "RMA", "crest1");
        FootballDataTeamDto awayTeam = new FootballDataTeamDto(2, "Barcelona", "BAR", "BAR", "crest2");

        FootballDataScoreTimeDto fullTimeScore = mock(FootballDataScoreTimeDto.class);
        when(fullTimeScore.getHome()).thenReturn(2);
        when(fullTimeScore.getAway()).thenReturn(1);

        FootballDataScoreDto score = mock(FootballDataScoreDto.class);
        when(score.getFullTime()).thenReturn(fullTimeScore);

        FootballDataMatchDto match = new FootballDataMatchDto("2023-10-28T15:00:00Z", homeTeam, awayTeam, score);
        List<FootballDataMatchDto> matches = List.of(match);

        // When
        String result = invokeConvertMatchesToString(matches);

        // Then
        String expected = "Real Madrid vs Barcelona (2023-10-28T15:00:00Z): 2 - 1; ";
        assertEquals(expected, result);
    }

    @Test
    void testConvertMatchesToString_withMatchesWithoutFullTimeScore_returnsPendingMatch() throws Exception {
        // Given
        FootballDataTeamDto homeTeam = new FootballDataTeamDto(1, "Athletic Bilbao", "ATH", "ATH", "crest1");
        FootballDataTeamDto awayTeam = new FootballDataTeamDto(2, "Valencia", "VAL", "VAL", "crest2");

        FootballDataScoreDto score = mock(FootballDataScoreDto.class);
        when(score.getFullTime()).thenReturn(null);

        FootballDataMatchDto match = new FootballDataMatchDto("2023-11-05T20:00:00Z", homeTeam, awayTeam, score);
        List<FootballDataMatchDto> matches = List.of(match);

        // When
        String result = invokeConvertMatchesToString(matches);

        // Then
        String expected = "Athletic Bilbao vs Valencia (2023-11-05T20:00:00Z): Pendiente; ";
        assertEquals(expected, result);
    }

    @Test
    void testConvertMatchesToString_withMultipleMatches_returnsAllMatchesFormatted() throws Exception {
        // Given
        FootballDataTeamDto team1 = new FootballDataTeamDto(1, "Sevilla", "SEV", "SEV", "crest1");
        FootballDataTeamDto team2 = new FootballDataTeamDto(2, "Atletico Madrid", "ATM", "ATM", "crest2");
        FootballDataTeamDto team3 = new FootballDataTeamDto(3, "Villarreal", "VIL", "VIL", "crest3");

        // First match with score
        FootballDataScoreTimeDto fullTimeScore1 = mock(FootballDataScoreTimeDto.class);
        when(fullTimeScore1.getHome()).thenReturn(1);
        when(fullTimeScore1.getAway()).thenReturn(3);

        FootballDataScoreDto score1 = mock(FootballDataScoreDto.class);
        when(score1.getFullTime()).thenReturn(fullTimeScore1);

        FootballDataMatchDto match1 = new FootballDataMatchDto("2023-10-15T17:30:00Z", team1, team2, score1);

        // Second match without score
        FootballDataScoreDto score2 = mock(FootballDataScoreDto.class);
        when(score2.getFullTime()).thenReturn(null);

        FootballDataMatchDto match2 = new FootballDataMatchDto("2023-10-22T19:45:00Z", team2, team3, score2);

        List<FootballDataMatchDto> matches = Arrays.asList(match1, match2);

        // When
        String result = invokeConvertMatchesToString(matches);

        // Then
        String expected = "Sevilla vs Atletico Madrid (2023-10-15T17:30:00Z): 1 - 3; " +
                         "Atletico Madrid vs Villarreal (2023-10-22T19:45:00Z): Pendiente; ";
        assertEquals(expected, result);
    }

    private String invokeConvertMatchesToString(List<FootballDataMatchDto> matches) throws Exception {
        Method method = MatchService.class.getDeclaredMethod("convertMatchesToString", List.class);
        method.setAccessible(true);
        return (String) method.invoke(matchService, matches);
    }

    @Test
    void getMatchPredictionBetween_InvalidTeamA_ThrowsException() {
        String team1Name = "";
        String team2Name = "Invalid Team 2";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> matchService.getMatchPredictionBetween(team1Name, team2Name));
    }

    @Test
    void getMatchPredictionBetween_InvalidTeamB_ThrowsException() {
        String team1Name = "Valid Team 1";
        String team2Name = "";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> matchService.getMatchPredictionBetween(team1Name, team2Name));
    }

    @Test
    void getMatchPredictionBetween_NullTeams_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> matchService.getMatchPredictionBetween(null, null));
    }

    @Test
    void getMatchPredictionBetween_InvalidTeamNames_ThrowsException() {
        String team1Name = "River Plate"; // Example of a team not in La Liga
        String team2Name = "Mallorca";


        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> matchService.getMatchPredictionBetween(team1Name, team2Name));
    }
}
