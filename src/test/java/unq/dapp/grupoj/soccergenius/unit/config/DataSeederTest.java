package unq.dapp.grupoj.soccergenius.unit.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import unq.dapp.grupoj.soccergenius.config.DataSeeder;
import unq.dapp.grupoj.soccergenius.model.dtos.auth.RegisterFormDTO;
import unq.dapp.grupoj.soccergenius.repository.PlayerRepository;
import unq.dapp.grupoj.soccergenius.repository.TeamRepository;
import unq.dapp.grupoj.soccergenius.repository.UsersRepository;
import unq.dapp.grupoj.soccergenius.services.auth.AuthService;
import unq.dapp.grupoj.soccergenius.services.player.PlayerService;
import unq.dapp.grupoj.soccergenius.services.team.TeamService;

import static org.mockito.Mockito.*;

@ActiveProfiles("unit")
@Tag("unit")
class DataSeederTest {
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private TeamService teamService;
    @Mock
    private AuthService authService;
    @Mock
    private PlayerService playerService;

    @InjectMocks
    private DataSeeder dataSeeder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dataSeeder = new DataSeeder(teamService, authService, playerService, teamRepository, usersRepository, playerRepository);
    }

    @Test
    void testRun_UserDoesNotExist_RegistersUser() {
        when(usersRepository.existsByEmail("john.doe@mail.com")).thenReturn(false);
        when(teamRepository.existsById(anyInt())).thenReturn(true);
        when(playerRepository.existsById(anyInt())).thenReturn(true);

        dataSeeder.run();

        verify(authService, times(1)).register(any(RegisterFormDTO.class));
    }

    @Test
    void testRun_UserExists_DoesNotRegisterUser() {
        when(usersRepository.existsByEmail("john.doe@mail.com")).thenReturn(true);
        when(teamRepository.existsById(anyInt())).thenReturn(true);
        when(playerRepository.existsById(anyInt())).thenReturn(true);

        dataSeeder.run();

        verify(authService, never()).register(any(RegisterFormDTO.class));
    }

    @Test
    void testRun_TeamDoesNotExist_CallsGetTeamFromLaLigaById() {
        when(usersRepository.existsByEmail(anyString())).thenReturn(true);
        when(teamRepository.existsById(anyInt())).thenReturn(false);
        when(playerRepository.existsById(anyInt())).thenReturn(true);

        dataSeeder.run();

        verify(teamService, atLeastOnce()).getTeamFromLaLigaById(anyInt());
    }

    @Test
    void testRun_PlayerDoesNotExist_SavesPlayer() {
        when(usersRepository.existsByEmail(anyString())).thenReturn(true);
        when(teamRepository.existsById(anyInt())).thenReturn(true);
        when(playerRepository.existsById(365409)).thenReturn(false);
        when(playerService.getPlayer(365409)).thenReturn(null);

        dataSeeder.run();

        verify(playerService, times(1)).getPlayer(365409);
        verify(playerRepository, times(1)).save(any());
    }
}