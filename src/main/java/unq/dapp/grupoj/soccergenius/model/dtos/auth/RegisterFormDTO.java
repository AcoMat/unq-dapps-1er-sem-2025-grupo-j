package unq.dapp.grupoj.soccergenius.model.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Data required for user registration")
public class RegisterFormDTO {
    @NotBlank(message = "First name is required")
    @Size(max = 25, message = "First name must be less than 25 characters")
    @Schema(description = "User's first name (max 25 characters)", example = "John")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 25, message = "Last name must be less than 25 characters")
    @Schema(description = "User's last name (max 25 characters)", example = "Doe")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;
    
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Schema(description = "User's password (min 6 characters)", example = "securePassword123")
    private String password;
}
