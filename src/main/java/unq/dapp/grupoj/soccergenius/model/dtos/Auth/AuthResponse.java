package unq.dapp.grupoj.soccergenius.model.dtos.Auth;

import lombok.Getter;
import unq.dapp.grupoj.soccergenius.model.dtos.UserDTO;

@Getter
public class AuthResponse {
    private final UserDTO user;
    private final String token;

    public AuthResponse(UserDTO user, String token) {
        this.user = user;
        this.token = token;
    }
}