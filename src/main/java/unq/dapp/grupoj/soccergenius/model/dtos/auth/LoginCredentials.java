package unq.dapp.grupoj.soccergenius.model.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Credentials required for user login")
public class LoginCredentials {
    @Email(message = "Email is not valid")
    @Schema(description = "User's email address", example = "user@example.com")
    private String email;
    
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Schema(description = "User's password (min 6 characters)", example = "password123")
    private String password;
}
