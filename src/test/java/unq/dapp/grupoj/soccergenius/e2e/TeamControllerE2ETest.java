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
// Assuming these DTOs are used by your controller. Adjust if necessary.
// import unq.dapp.grupoj.soccergenius.model.dtos.MatchDTO;
// import unq.dapp.grupoj.soccergenius.model.dtos.TeamDto;
// The ScrappingException might be relevant if your controller handles it.
// import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;

import java.util.List; // For jsonPath type checking

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

    /*
    @Test
    @WithMockUser
    public void getMatchesByTeamId_whenTeamExistsAndHasMatches_shouldReturnMatchList() throws Exception {
        // TODO: Ensure a team (e.g., ID 1L) exists and has associated matches in the database.
        long teamIdWithMatches = 1L; // Example ID

        mockMvc.perform(get("/api/teams/" + teamIdWithMatches + "/matches") // Adjust endpoint
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", instanceOf(List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThan(0)))); // Assumes there's at least one match
        // .andExpect(jsonPath("$[0].someMatchProperty", is("expectedValue"))); // Check match details
    }

    @Test
    @WithMockUser
    public void getMatchesByTeamId_whenTeamExistsAndHasNoMatches_shouldReturnEmptyList() throws Exception {
        // TODO: Ensure a team (e.g., ID 2L) exists but has no associated matches.
        long teamIdWithNoMatches = 2L; // Example ID

        mockMvc.perform(get("/api/teams/" + teamIdWithNoMatches + "/matches") // Adjust endpoint
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser
    public void getMatchesByTeamId_whenTeamDoesNotExist_shouldReturnNotFound() throws Exception {
        long nonExistentTeamId = 9999L;

        mockMvc.perform(get("/api/teams/" + nonExistentTeamId + "/matches") // Adjust endpoint
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Assuming 404 if team not found
    }

    @Test
    @WithMockUser
    public void searchTeamsByName_whenMatchesFound_shouldReturnTeamList() throws Exception {
        // TODO: Ensure teams exist that match the search query (e.g., "United").
        String searchQuery = "United"; // Example search query

        mockMvc.perform(get("/api/teams/search") // Adjust endpoint and param name if different
                        .param("name", searchQuery)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", instanceOf(List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].name", containsStringIgnoringCase(searchQuery)));
    }

    @Test
    @WithMockUser
    public void searchTeamsByName_whenNoMatchesFound_shouldReturnEmptyList() throws Exception {
        String searchQuery = "ThisNameShouldNotExistInDB";

        mockMvc.perform(get("/api/teams/search") // Adjust endpoint
                        .param("name", searchQuery)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser
    public void getMatchesByTeamId_whenScrappingErrorOccurs_shouldReturnAppropriateError() throws Exception {
        // This test assumes there's a specific scenario (e.g., a particular teamId or external service state)
        // that would cause the actual TeamService to throw a ScrappingException,
        // and that your controller (or a global exception handler) translates this
        // to a specific HTTP status (e.g., 503 Service Unavailable).
        long teamIdKnownToCauseScrappingError = 777L; // Hypothetical ID

        // This test's success depends heavily on your application's ability to simulate this error.
        // It might be more suitable for integration tests with controlled external dependencies.
        mockMvc.perform(get("/api/teams/" + teamIdKnownToCauseScrappingError + "/matches")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable()); // Or 500, or whatever status you map ScrappingException to.
    }
     */
}
