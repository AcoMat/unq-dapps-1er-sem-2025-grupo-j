package unq.dapp.grupoj.soccergenius.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import unq.dapp.grupoj.soccergenius.exceptions.TeamNotFoundException;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("e2e")
@Tag("e2e")
@SpringBootTest
@AutoConfigureMockMvc
public class TeamControllerE2ETest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser
    public void getTeamById_whenTeamExists_shouldReturnTeamDto() throws Exception {
        long existingTeamId = 839;

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
    public void getTeamById_whenTeamDoesNotExist_shouldReturnNotFound() throws Exception {
        long nonExistentTeamId = 0;

        final ResultActions result = this.mockMvc.perform(get("/teams/" + nonExistentTeamId)
                        .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound())
                .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof TeamNotFoundException));
    }

    @Test
    @WithMockUser
    public void getTeamPlayers_whenTeamExists_shouldReturnPlayerList() throws Exception {
        final ResultActions result = this.mockMvc.perform(get("/teams/river/argentina/players")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$", instanceOf(List.class)));
        result.andExpect(jsonPath("$", hasSize(greaterThan(0))));
        result.andExpect(jsonPath("$[0]", instanceOf(String.class)));
    }

    @Test
    @WithMockUser
    public void getTeamPlayers_whenTeamDoesNotExist_shouldReturnPlayerListEmpty() throws Exception {
        long nonExistentTeamId = 0;

        final ResultActions result = this.mockMvc.perform(get("/teams/" + nonExistentTeamId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
        result.andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof TeamNotFoundException));
    }

    @Test
    @WithMockUser
    public void getUpcomingMatches_whenTeamExists_shouldReturnTeamDto() throws Exception {
        final ResultActions result = this.mockMvc.perform(get("/teams/getafe/upcomingMatches")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$", instanceOf(List.class)));
    }

    @Test
    @WithMockUser
    public void getUpcomingMatches_whenTeamDoesNotExist_shouldReturnTeamDto() throws Exception {
        final ResultActions result = this.mockMvc.perform(get("/teams/getafes/upcomingMatches")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$", instanceOf(List.class)));
    }
}
