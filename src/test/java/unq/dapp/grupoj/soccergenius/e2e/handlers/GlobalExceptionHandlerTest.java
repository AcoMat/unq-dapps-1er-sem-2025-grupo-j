package unq.dapp.grupoj.soccergenius.e2e.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import unq.dapp.grupoj.soccergenius.repository.UsersRepository;
import unq.dapp.grupoj.soccergenius.model.AppUser;
import org.junit.jupiter.api.BeforeEach;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("e2e")
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @BeforeEach
    void ensureTestUserExists() {
        String email = "john.doe@mail.com";
        if (!usersRepository.existsByEmail(email)) {
            AppUser user = new AppUser("John", "Doe", email, "securePassword123");
            usersRepository.save(user);
        }
    }

    @Test
    @WithMockUser
    void testHandleResourceNotFound() throws Exception {
        mockMvc.perform(get("/non-existent-endpoint"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("This endpoint does not exist, verify the URL")));
    }

    @Test
    @WithMockUser
    void testRegisterWithAlreadyUsedEmail() throws Exception {
        String validRegistrationJson = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "john.doe@mail.com",
                    "password": "securePassword123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRegistrationJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("The email")))
                .andExpect(content().string(containsString("already in use")));
    }

    @Test
    @WithMockUser
    void testHandleRegisterValidationExceptions() throws Exception {
        String invalidJson = """
                {
                    "firstName": "",
                    "lastName": "Doe",
                    "email": "not-an-email",
                    "password": ""
                }
                """;
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("firstName")))
                .andExpect(content().string(containsString("password")))
                .andExpect(content().string(containsString("email")));
    }



    @Test
    @WithMockUser
    void testHandleNotReadableHttp() throws Exception {
        String malformedJson = "{";
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid body format")));
    }

    @Test
    @WithMockUser
    void testLoginWithNonExistingEmailAndWrongPassword() throws Exception {
        String loginJson = """
                {
                    "email": "john.doe@mail.com",
                    "password": "wrongPassword123"
                }
                """;
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email or password is incorrect"))
                );
    }

    @Test
    void testProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/players/performance/303655"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid or missing authentication token")));
    }


}