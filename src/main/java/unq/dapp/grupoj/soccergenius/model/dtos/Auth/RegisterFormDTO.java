package unq.dapp.grupoj.soccergenius.model.dtos.Auth;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterFormDTO {
    @NotBlank(message = "First name is required")
    @Size(max = 25, message = "First name must be less than 25 characters")
    private String firstName;
    @NotBlank(message = "Last name is required")
    @Size(max = 25, message = "Last name must be less than 25 characters")
    private String lastName;
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
}

