package unq.dapp.grupoj.soccergenius.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import unq.dapp.grupoj.soccergenius.model.Player;
import unq.dapp.grupoj.soccergenius.security.JwtTokenProvider;
import unq.dapp.grupoj.soccergenius.services.team.TeamService;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TeamControllerTest {

    private TeamService teamService;
    private TeamController teamController;
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        teamService = mock(TeamService.class);
        jwtTokenProvider = mock(JwtTokenProvider.class); // Mock the instance
        teamController = new TeamController(teamService, jwtTokenProvider);

        // Mock token validation to avoid exceptions during tests
        doNothing().when(jwtTokenProvider).validateToken(anyString());
    }

    @Test
    void testGetTeamPlayersSuccess() {
        // Arrange
        String teamName = "TeamA";
        String country = "CountryA";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "valid-token");

        List<Player> mockPlayers = Arrays.asList(
                new Player("Player1", "10", "5", "3", "7.5"),
                new Player("Player2", "15", "8", "6", "8.0")
        );

        when(teamService.getTeamPlayers(teamName, country)).thenReturn(mockPlayers);

        // Act
        ResponseEntity<List<Player>> response = teamController.getTeamPlayers(teamName, country, headers);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("Player1", response.getBody().getFirst().getName());
        verify(teamService, times(1)).getTeamPlayers(teamName, country);
        verify(jwtTokenProvider, times(1)).validateToken("valid-token");
    }

    @Test
    void testGetTeamPlayersUnauthorized() {
        // Arrange
        String teamName = "TeamA";
        String country = "CountryA";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "invalid-token");

        // Simulate token validation failure
        doThrow(new RuntimeException("Invalid token")).when(jwtTokenProvider).validateToken("invalid-token");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teamController.getTeamPlayers(teamName, country, headers);
        });

        assertEquals("Invalid token", exception.getMessage());
        verifyNoInteractions(teamService);
        verify(jwtTokenProvider, times(1)).validateToken("invalid-token");
    }

    @Test
    void testGetTeamPlayersServiceError() {
        // Arrange
        String teamName = "TeamA";
        String country = "CountryA";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "valid-token");

        when(teamService.getTeamPlayers(teamName, country)).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teamController.getTeamPlayers(teamName, country, headers);
        });

        assertEquals("Service error", exception.getMessage());
        verify(teamService, times(1)).getTeamPlayers(teamName, country);
        verify(jwtTokenProvider, times(1)).validateToken("valid-token");
    }
}
