package unq.dapp.grupoj.soccergenius.model.dtos.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginCredentials {
    @Email(message = "Email is not valid")
    private String email;
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
}

