package unq.dapp.grupoj.soccergenius.config;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import unq.dapp.grupoj.soccergenius.model.dtos.auth.RegisterFormDTO;
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.repository.PlayerRepository;
import unq.dapp.grupoj.soccergenius.repository.TeamRepository;
import unq.dapp.grupoj.soccergenius.repository.UsersRepository;
import unq.dapp.grupoj.soccergenius.services.auth.AuthService;
import unq.dapp.grupoj.soccergenius.services.player.PlayerService;
import unq.dapp.grupoj.soccergenius.services.team.TeamService;

import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final TeamRepository teamRepository;
    private final UsersRepository usersRepository;
    private final PlayerRepository playerRepository;

    private final TeamService teamService;
    private final AuthService authService;
    private final PlayerService playerService;
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    public DataSeeder(
            TeamService teamService,
            AuthService authService,
            PlayerService playerService,
            TeamRepository teamRepository,
            UsersRepository usersRepository,
            PlayerRepository playerRepository
    ) {
        this.teamService = teamService;
        this.authService = authService;
        this.playerService = playerService;
        this.teamRepository = teamRepository;
        this.usersRepository = usersRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Seeding data...");

        if(usersRepository.existsByEmail("john.doe@mail.com")) {
            logger.info("User already exists, skipping registration.");
        } else {
            RegisterFormDTO registerFormDTO = new RegisterFormDTO("John", "Doe", "john.doe@mail.com", "password123");
            authService.register(registerFormDTO);
        }
        List<Integer> teamIds = Arrays.asList(63,51,52,53,54,55,58,60,62,63,65,64,67,68,70,131,839,819,825,838,2783);
        for (Integer teamId : teamIds) {
            if (!teamRepository.existsById(teamId)) {
                teamService.getTeamFromLaLigaById(teamId);
            } else {
                logger.info("Team with ID {} already exists, skipping registration.", teamId);
            }
        }

        if(playerRepository.existsById(365409)) {
            logger.info("Player already exists, skipping registration.");
        } else {
            Player player = playerService.getPlayer(365409);
            playerRepository.save(player);
        }
        logger.info("Data seeding completed.");
    }
}
