package unq.dapp.grupoj.soccergenius.services.implementation;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.exceptions.AlreadyUsedEmail;
import unq.dapp.grupoj.soccergenius.mappers.Mapper;
import unq.dapp.grupoj.soccergenius.model.dtos.Auth.AuthResponse;
import unq.dapp.grupoj.soccergenius.model.dtos.Auth.LoginCredentials;
import unq.dapp.grupoj.soccergenius.model.dtos.Auth.RegisterFormDTO;
import unq.dapp.grupoj.soccergenius.model.AppUser;
import unq.dapp.grupoj.soccergenius.repository.UsersRepository;
import unq.dapp.grupoj.soccergenius.security.JwtTokenProvider;

@Service
public class AuthService {
    private final UsersRepository usersRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UsersRepository usersRepository, Mapper mapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.usersRepository = usersRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    public AuthResponse register(RegisterFormDTO registerData) {

        if (usersRepository.existsByEmail(registerData.getEmail())) {
            throw new AlreadyUsedEmail(registerData.getEmail());
        }

        AppUser new_app_user = mapper.toEntity(registerData);
        new_app_user.setPassword(passwordEncoder.encode(registerData.getPassword()));

        usersRepository.save(new_app_user);

        return new AuthResponse(mapper.toDTO(new_app_user), jwtTokenProvider.generateToken(new_app_user.getId()));
    }

    public AuthResponse login(LoginCredentials credentials){
        AppUser appUser = usersRepository.findByEmail(credentials.getEmail());
        if (appUser == null || !passwordEncoder.matches(credentials.getPassword(), appUser.getPassword())) {
            throw new IllegalArgumentException("Email or password is incorrect");
        }

        return new AuthResponse(mapper.toDTO(appUser), jwtTokenProvider.generateToken(appUser.getId()));
    }
}
