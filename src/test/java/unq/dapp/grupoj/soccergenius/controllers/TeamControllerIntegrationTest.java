package unq.dapp.grupoj.soccergenius.controllers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
import unq.dapp.grupoj.soccergenius.security.JwtAuthenticationFilter;
import unq.dapp.grupoj.soccergenius.services.history_log.HistoryLogService;
import unq.dapp.grupoj.soccergenius.services.team.TeamService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Disabled
@WebMvcTest(TeamController.class)
public class TeamControllerIntegrationTest {
    @TestConfiguration
    static class TestConfig {

        @Bean
        public HistoryLogService historyLogService() {
            // Retorna un mock de HistoryLogService
            return Mockito.mock(HistoryLogService.class);
        }

        @Bean // Define TeamService como un bean mock
        public TeamService teamService() {
            return Mockito.mock(TeamService.class);
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter(){
            return Mockito.mock(JwtAuthenticationFilter.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Mock // Este mock es para el TeamService, que es una dependencia directa del TeamController
    private TeamService teamService;

    @Test
    @WithMockUser // Para simular un usuario autenticado y evitar el 401 por SecurityRequirement
    public void getTeamPlayers_whenTeamExistsAndPlayersFound_shouldReturnOkAndPlayerList() throws Exception {
        // Arrange
        String teamName = "Barcelona";
        String country = "Spain";
        List<String> mockPlayers = Arrays.asList(
                "Player1 - Games: 10, Goals: 5, Assists: 3, Rating: 8.5",
                "Player2 - Games: 12, Goals: 2, Assists: 7, Rating: 7.9"
        );

        when(teamService.getTeamPlayers(teamName, country)).thenReturn(mockPlayers);

        // Act & Assert
        mockMvc.perform(get("/teams/{teamName}/{country}/players", teamName, country)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", is("Player1 - Games: 10, Goals: 5, Assists: 3, Rating: 8.5")))
                .andExpect(jsonPath("$[1]", is("Player2 - Games: 12, Goals: 2, Assists: 7, Rating: 7.9")));
    }

    @Test
    @WithMockUser
    public void getTeamPlayers_whenTeamExistsAndNoPlayersFound_shouldReturnOkAndEmptyList() throws Exception {
        // Arrange
        String teamName = "EmptyTeam";
        String country = "Nowhere";
        List<String> mockPlayers = Collections.emptyList();

        when(teamService.getTeamPlayers(teamName, country)).thenReturn(mockPlayers);

        // Act & Assert
        mockMvc.perform(get("/teams/{teamName}/{country}/players", teamName, country)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser
    public void getTeamPlayers_whenScrappingErrorOccurs_shouldReturnInternalServerError() throws Exception {
        // Arrange
        String teamName = "ErrorTeam";
        String country = "ProblemLand";
        String errorMessage = "Failed to scrape player data";

        when(teamService.getTeamPlayers(teamName, country)).thenThrow(new ScrappingException(errorMessage));

        // Act & Assert
        mockMvc.perform(get("/teams/{teamName}/{country}/players", teamName, country)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(errorMessage)));
    }

    @Test
    @WithMockUser
    public void getTeamPlayers_whenTeamNameContainsNewlines_shouldReplaceThemAndCallService() throws Exception {
        // Arrange
        String teamNameWithNewline = "Barcelona\nFC";
        String country = "Spain";
        String expectedTeamName = "Barcelona_FC"; // Como se espera que se limpie en el controlador
        List<String> mockPlayers = Arrays.asList("PlayerX");

        when(teamService.getTeamPlayers(expectedTeamName, country)).thenReturn(mockPlayers);

        // Act & Assert
        mockMvc.perform(get("/teams/{teamName}/{country}/players", teamNameWithNewline, country)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]", is("PlayerX")));
    }

    @Test
    @WithMockUser
    public void getTeamPlayers_whenCountryContainsNewlines_shouldReplaceThemAndCallService() throws Exception {
        // Arrange
        String teamName = "RealMadrid";
        String countryWithNewline = "Sp\rain";
        String expectedCountry = "Sp_ain"; // Como se espera que se limpie en el controlador
        List<String> mockPlayers = Arrays.asList("PlayerY");

        when(teamService.getTeamPlayers(teamName, expectedCountry)).thenReturn(mockPlayers);

        // Act & Assert
        mockMvc.perform(get("/teams/{teamName}/{country}/players", teamName, countryWithNewline)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]", is("PlayerY")));
    }

}
