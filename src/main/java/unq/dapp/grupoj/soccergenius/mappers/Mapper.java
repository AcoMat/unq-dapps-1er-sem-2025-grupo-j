package unq.dapp.grupoj.soccergenius.mappers;

import org.springframework.stereotype.Component;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamDto;
import unq.dapp.grupoj.soccergenius.model.dtos.auth.RegisterFormDTO;
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

    public TeamDto toDTO(Team team) {
        return new TeamDto(
            team.getName(),
            team.getCountry(),
            team.getLeague());
    }
}
