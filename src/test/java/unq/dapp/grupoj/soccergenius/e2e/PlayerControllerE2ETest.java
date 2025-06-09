package unq.dapp.grupoj.soccergenius.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.repository.PlayerRepository;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PlayerControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlayerRepository playerRepository;

    private Player testPlayer;


    @BeforeEach
    void setUp() {
        playerRepository.deleteAll();
        Player player = new Player();
        this.testPlayer = playerRepository.save(player);
    }

    @Test
    @WithMockUser
    void getPlayerPerformance_whenPlayerExists_shouldReturnOkAndPerformanceData() throws Exception {
        mockMvc.perform(get("/players/performance/29400", testPlayer.getId())
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    @WithMockUser
    void getPlayerPerformance_whenPlayerNotExists_shouldReturnStringNotFoundPlayer() throws Exception {
        mockMvc.perform(get("/players/performance/294001", testPlayer.getId())
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Player not found"));
    }

    @Test
    @WithMockUser
    void getPlayerById_whenPlayerExists_shouldReturnPlayerDTO() throws Exception {
        mockMvc.perform(get("/players/29400", testPlayer.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(29400)))
                .andExpect(jsonPath("$.name", is("Robert Lewandowski")));
    }

    @Test
    @WithMockUser
    void getPlayerById_whenPlayerNotExists_shouldReturnPlayerNotFound() throws Exception {
        final ResultActions result = this.mockMvc.perform(get("/players/1", testPlayer.getId())
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().is5xxServerError());
    }
}
