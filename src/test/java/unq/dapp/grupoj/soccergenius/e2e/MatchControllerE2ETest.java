package unq.dapp.grupoj.soccergenius.e2e;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("e2e")
@Tag("e2e")
@AutoConfigureMockMvc
@SpringBootTest()
public class MatchControllerE2ETest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void getMatchPredictionBetween_realMadrid_vs_barcelona() throws Exception {
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
}
