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
    public void run(String... args) throws Exception {
        logger.info("Seeding data...");

        if(usersRepository.existsByEmail("john.doe@mail.com")) {
            logger.info("User already exists, skipping registration.");
        } else {
            RegisterFormDTO registerFormDTO = new RegisterFormDTO("John", "Doe", "john.doe@mail.com", "password123");
            authService.register(registerFormDTO);
        }

        if(teamRepository.existsById(63)) {
            logger.info("Team already exists, skipping registration.");
        } else {
            if(!teamRepository.existsById(51)) {
                teamService.getTeamFromLaLigaById(51);
            }
            if(!teamRepository.existsById(52)) {
                teamService.getTeamFromLaLigaById(52);
            }
            if(!teamRepository.existsById(53)) {
                teamService.getTeamFromLaLigaById(53);
            }
            if(!teamRepository.existsById(54)) {
                teamService.getTeamFromLaLigaById(54);
            }
            if(!teamRepository.existsById(55)) {
                teamService.getTeamFromLaLigaById(55);
            }
            if(!teamRepository.existsById(58)) {
                teamService.getTeamFromLaLigaById(58);
            }
            if(!teamRepository.existsById(60)) {
                teamService.getTeamFromLaLigaById(60);
            }
            if(!teamRepository.existsById(62)) {
                teamService.getTeamFromLaLigaById(62);
            }
            if(!teamRepository.existsById(63)) {
                teamService.getTeamFromLaLigaById(63);
            }
            if(!teamRepository.existsById(65)) {
                teamService.getTeamFromLaLigaById(65);
            }
            if(!teamRepository.existsById(64)) {
                teamService.getTeamFromLaLigaById(64);
            }
            if(!teamRepository.existsById(67)) {
                teamService.getTeamFromLaLigaById(67);
            }
            if(!teamRepository.existsById(68)) {
                teamService.getTeamFromLaLigaById(68);
            }
            if(!teamRepository.existsById(70)) {
                teamService.getTeamFromLaLigaById(70);
            }
            if(!teamRepository.existsById(131)) {
                teamService.getTeamFromLaLigaById(131);
            }
            if(!teamRepository.existsById(839)) {
                teamService.getTeamFromLaLigaById(839);
            }
            if(!teamRepository.existsById(819)) {
                teamService.getTeamFromLaLigaById(819);
            }
            if(!teamRepository.existsById(825)) {
                teamService.getTeamFromLaLigaById(825);
            }
            if(!teamRepository.existsById(838)) {
                teamService.getTeamFromLaLigaById(838);
            }
            if(!teamRepository.existsById(2783)) {
                teamService.getTeamFromLaLigaById(2783);
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
