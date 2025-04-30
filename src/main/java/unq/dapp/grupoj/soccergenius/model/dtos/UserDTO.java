package unq.dapp.grupoj.soccergenius.model.dtos;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDTO {
    private String firstName;
    private String lastName;
    private String email;
}