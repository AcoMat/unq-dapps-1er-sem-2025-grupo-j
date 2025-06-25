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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import unq.dapp.grupoj.soccergenius.repository.PlayerRepository;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("e2e")
@Tag("e2e")
@SpringBootTest
@AutoConfigureMockMvc
public class PlayerControllerE2ETest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PlayerRepository playerRepository;

    @BeforeEach
    void setUp() {
        playerRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void getPlayerPerformance_whenPlayerExists_shouldReturnOkAndPerformanceData() throws Exception {
        int lewandowskiId = 29400;
        mockMvc.perform(get("/players/performance/" + lewandowskiId)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    @WithMockUser
    void getPlayerPerformance_whenPlayerNotExists_shouldReturnStringNotFoundPlayer() throws Exception {
        int unknowId = 999999;
        mockMvc.perform(get("/players/performance/" + unknowId)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Player not found"));
    }

    @Test
    @WithMockUser
    void getPlayerById_whenPlayerExists_shouldReturnPlayerDTO() throws Exception {
        int lewandowskiId = 29400;
        mockMvc.perform(get("/players/" + lewandowskiId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(lewandowskiId)))
                .andExpect(jsonPath("$.name", is("Robert Lewandowski")));
    }

    @Test
    @WithMockUser
    void getPlayerById_whenPlayerNotExists_shouldReturnPlayerNotFound() throws Exception {
        int unknowId = 999999;
        final ResultActions result = this.mockMvc.perform(get("/players/" + unknowId)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().is4xxClientError());
    }
}
