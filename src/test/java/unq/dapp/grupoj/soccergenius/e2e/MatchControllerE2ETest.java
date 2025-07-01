package unq.dapp.grupoj.soccergenius.e2e;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamDto;
import unq.dapp.grupoj.soccergenius.model.dtos.external.football_data.FootballDataMatchsDto;
import unq.dapp.grupoj.soccergenius.services.external.football_data.FootballDataApiService;
import unq.dapp.grupoj.soccergenius.services.external.whoscored.TeamScrapingService;
import unq.dapp.grupoj.soccergenius.services.team.TeamService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("e2e")
@Tag("e2e")
@AutoConfigureMockMvc
@SpringBootTest()
class MatchControllerE2ETest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private TeamScrapingService teamScrapingService;
    @MockitoBean
    private FootballDataApiService footballDataApiService;
    @MockitoBean
    private TeamService teamService;

    @Test
    @WithMockUser
    void getMatchPredictionBetween_realMadrid_vs_barcelona() throws Exception {
        when(teamService.getTeamFromLaLigaById(52)).thenReturn(new TeamDto( "Real Madrid", "España", "LaLiga")); // Real Madrid
        when(teamService.getTeamFromLaLigaById(65)).thenReturn(new TeamDto( "Barcelona", "España", "LaLiga")); // Barcelona
        when(teamScrapingService.getCurrentRankingOfTeam(52)).thenReturn(6.85); // Real Madrid
        when(teamScrapingService.getCurrentRankingOfTeam(65)).thenReturn(6.86); // Barcelona
        when(teamScrapingService.getCurrentPositionOnLeague(52)).thenReturn(1); // Real Madrid
        when(teamScrapingService.getCurrentPositionOnLeague(65)).thenReturn(2); // Barcelona
        //Vacios para evitar complejidad
        when(footballDataApiService.getLastXMatchesFromTeam(86, 5)).thenReturn(new FootballDataMatchsDto(Collections.emptyList())); // Real Madrid
        when(footballDataApiService.getLastXMatchesFromTeam(81, 5)).thenReturn(new FootballDataMatchsDto(Collections.emptyList())); // Barcelona


        String teamAName = "Real Madrid";
        String teamBName = "Barcelona";
        mockMvc.perform(get("/matches/prediction")
                        .param("teamAName", teamAName)
                        .param("teamBName", teamBName)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(notNullValue()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    assertThat(response).isNotEmpty();
                    assertThat(response).contains(":");
                });
    }

    @Test
    @WithMockUser
    void getMatchPredictionBetween_invalidTeams() throws Exception {
        String teamAName = "Invalid Team A";
        String teamBName = "Invalid Team B";

        mockMvc.perform(get("/matches/prediction")
                        .param("teamAName", teamAName)
                        .param("teamBName", teamBName)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(notNullValue()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    assertThat(response).contains("No team found");
                });
    }


}
