package unq.dapp.grupoj.soccergenius.e2e;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import unq.dapp.grupoj.soccergenius.exceptions.TeamNotFoundException;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamStatisticsDTO;
import unq.dapp.grupoj.soccergenius.repository.TeamRepository;
import unq.dapp.grupoj.soccergenius.services.external.whoscored.TeamScrapingService;

import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("e2e")
@Tag("e2e")
@SpringBootTest
@AutoConfigureMockMvc
class TeamControllerE2ETest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @MockitoBean
    private TeamScrapingService teamScrapingService;
    @Autowired
    private TeamRepository teamRepository;


    @BeforeEach
    void setup() {
        teamRepository.deleteAll();
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser
    void getTeamById_whenTeamExists_shouldReturnTeamDto() throws Exception {
        long existingTeamId = 839;
        Team mockedTeam = new Team((int) existingTeamId, "Villarreal", "España", "LaLiga");
        when(teamScrapingService.scrapTeamDataById((int) existingTeamId)).thenReturn(mockedTeam);

        final ResultActions result = this.mockMvc.perform(get("/teams/" + existingTeamId)
                        .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        result.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        result.andExpect(jsonPath("$.name", is("Villarreal")));
        result.andExpect(jsonPath("$.country", is("España")));
        result.andExpect(jsonPath("$.league", is("LaLiga")));
    }

    @Test
    @WithMockUser
    void getTeamById_whenTeamDoesNotExist_shouldReturnNotFound() throws Exception {
        long nonExistentTeamId = 0;
        when(teamScrapingService.scrapTeamDataById((int) nonExistentTeamId)).thenThrow(new TeamNotFoundException("Team not found"));

        final ResultActions result = this.mockMvc.perform(get("/teams/" + nonExistentTeamId)
                        .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound())
                .andExpect(mvcResult -> assertInstanceOf(TeamNotFoundException.class, mvcResult.getResolvedException()));
    }

    @Test
    @WithMockUser
    void getTeamPlayers_whenTeamExists_shouldReturnPlayerList() throws Exception {
        when(teamScrapingService.getPlayersNamesFromTeam("river", "argentina")).thenReturn(List.of(" Armani ", "Diaz"));

        final ResultActions result = this.mockMvc.perform(get("/teams/river/argentina/players")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$", instanceOf(List.class)));
        result.andExpect(jsonPath("$", hasSize(greaterThan(0))));
        result.andExpect(jsonPath("$[0]", instanceOf(String.class)));
    }

    @Test
    @WithMockUser
    void compareTeams_whenValidTeams_shouldReturnComparison() throws Exception {
        String team1 = "Real Madrid";
        String team2 = "Barcelona";
        int team1Id = 52;
        int team2Id = 65;
        // Arrange
        TeamStatisticsDTO team1Stats = new TeamStatisticsDTO(team1, "10", "20", "15", "60%", "85%", "70%", "5", "4", "2");
        TeamStatisticsDTO team2Stats = new TeamStatisticsDTO(team2, "12", "22", "18", "65%", "90%", "75%", "3", "1", "0");

        when(teamScrapingService.scrapTeamStatisticsById(team1Id)).thenReturn(team1Stats);
        when(teamScrapingService.scrapTeamStatisticsById(team2Id)).thenReturn(team2Stats);

        final ResultActions result = this.mockMvc.perform(get("/teams/comparison?teamAName=" + team1 + "&teamBName=" + team2)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        result.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        result.andExpect(jsonPath("$.teamA.name", is(team1)));
        result.andExpect(jsonPath("$.teamB.name", is(team2)));
    }

    /* TESTS DISABLED DUE API SUB CHANGES
    @Test
    @WithMockUser
    void getUpcomingMatches_whenTeamExists_shouldReturnTeamDto() throws Exception {
        final ResultActions result = this.mockMvc.perform(get("/teams/getafe/upcomingMatches")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$", instanceOf(List.class)));
    }

    @Test
    @WithMockUser
    void getUpcomingMatches_whenTeamDoesNotExist_shouldReturnTeamDto() throws Exception {
        final ResultActions result = this.mockMvc.perform(get("/teams/getafes/upcomingMatches")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$", instanceOf(List.class)));
    }
     */
}
