package unq.dapp.grupoj.soccergenius.mappers;

import org.springframework.stereotype.Component;
import unq.dapp.grupoj.soccergenius.model.dtos.Auth.RegisterFormDTO;
import unq.dapp.grupoj.soccergenius.model.dtos.UserDTO;
import unq.dapp.grupoj.soccergenius.model.AppUser;

@Component
public class Mapper {
    public AppUser toEntity(RegisterFormDTO userDTO) {
        return new AppUser(
                userDTO.getFirstName(),
                userDTO.getLastName(),
                userDTO.getEmail(),
                userDTO.getPassword()
        );
    }

    public UserDTO toDTO(AppUser appUser) {
        return new UserDTO(appUser.getFirstName(), appUser.getLastName(), appUser.getEmail());
    }
}
