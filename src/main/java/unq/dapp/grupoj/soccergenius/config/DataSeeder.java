package unq.dapp.grupoj.soccergenius.config;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import unq.dapp.grupoj.soccergenius.model.dtos.auth.RegisterFormDTO;
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.services.auth.AuthService;
import unq.dapp.grupoj.soccergenius.services.player.PlayerService;
import unq.dapp.grupoj.soccergenius.services.team.TeamService;

@Component
public class DataSeeder implements CommandLineRunner {

    private final TeamService teamService;
    private final AuthService authService;
    private final PlayerService playerService;
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    public DataSeeder(TeamService teamService, AuthService authService, PlayerService playerService) {
        this.teamService = teamService;
        this.authService = authService;
        this.playerService = playerService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        Player julianAlvarez = playerService.getPlayer(365409); //Julián Alvarez
        teamService.getTeamFromLaLigaById(63); //Atletico Madrid

        RegisterFormDTO registerFormDTO = new RegisterFormDTO("John", "Doe", "john.doe@mail.com", "password123");
        authService.register(registerFormDTO);

    }
}
