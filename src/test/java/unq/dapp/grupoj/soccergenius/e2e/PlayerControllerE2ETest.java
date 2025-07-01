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
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.model.player.summary.CurrentParticipationsSummary;
import unq.dapp.grupoj.soccergenius.model.player.summary.HistoricalParticipationsSummary;
import unq.dapp.grupoj.soccergenius.repository.PlayerRepository;
import unq.dapp.grupoj.soccergenius.services.external.whoscored.PlayerScrapingService;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("e2e")
@Tag("e2e")
@SpringBootTest
@AutoConfigureMockMvc
class PlayerControllerE2ETest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PlayerRepository playerRepository;

    @MockitoBean
    private PlayerScrapingService playerScrapingService;

    @BeforeEach
    void setUp() {
        playerRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void getPlayerPerformance_whenPlayerExists_shouldReturnOkAndPerformanceData() throws Exception {
        int lewandowskiId = 29400;
        Player lewandowski = new Player(lewandowskiId, "Robert Lewandowski", 35, "Poland", "185cm", Collections.singletonList("Forward"));
        CurrentParticipationsSummary currentSummary = new CurrentParticipationsSummary(lewandowski, 8.5);
        HistoricalParticipationsSummary historicalSummary = new HistoricalParticipationsSummary(lewandowski, 8.0);

        when(playerScrapingService.scrapPlayerData(lewandowskiId)).thenReturn(lewandowski);
        when(playerScrapingService.getCurrentParticipationInfo(lewandowski)).thenReturn(currentSummary);
        when(playerScrapingService.getHistoryInfo(lewandowski)).thenReturn(historicalSummary);

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
        when(playerScrapingService.scrapPlayerData(unknowId)).thenReturn(null);

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
        Player lewandowski = new Player(lewandowskiId, "Robert Lewandowski", 35, "Poland", "185cm", Collections.singletonList("Forward"));
        CurrentParticipationsSummary currentSummary = new CurrentParticipationsSummary(lewandowski, 8.5);
        HistoricalParticipationsSummary historicalSummary = new HistoricalParticipationsSummary(lewandowski, 8.0);

        when(playerScrapingService.scrapPlayerData(lewandowskiId)).thenReturn(lewandowski);
        when(playerScrapingService.getCurrentParticipationInfo(lewandowski)).thenReturn(currentSummary);
        when(playerScrapingService.getHistoryInfo(lewandowski)).thenReturn(historicalSummary);

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
        when(playerScrapingService.scrapPlayerData(unknowId)).thenReturn(null);

        final ResultActions result = this.mockMvc.perform(get("/players/" + unknowId)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().is4xxClientError());
    }
}
