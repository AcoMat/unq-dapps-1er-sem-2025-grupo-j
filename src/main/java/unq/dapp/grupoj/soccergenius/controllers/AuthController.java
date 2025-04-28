package unq.dapp.grupoj.soccergenius.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unq.dapp.grupoj.soccergenius.model.dtos.Auth.AuthResponse;
import unq.dapp.grupoj.soccergenius.model.dtos.Auth.LoginCredentials;
import unq.dapp.grupoj.soccergenius.model.dtos.Auth.RegisterFormDTO;
import unq.dapp.grupoj.soccergenius.model.dtos.UserDTO;
import unq.dapp.grupoj.soccergenius.services.auth.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user and return a JWT token")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterFormDTO data) {
        logger.info("Processing registration request for email: {}", data.getEmail());
        try {
            AuthResponse newUser = authService.register(data);
            logger.info("User registered successfully: {}", data.getEmail());
            return ResponseEntity.ok().header("Authorization", "Bearer " + newUser.getToken()).body(newUser.getUser());
        } catch (Exception e) {
            logger.error("Registration failed for email {}: {}", data.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user and return a JWT token")
    public ResponseEntity<UserDTO> login(@Valid @RequestBody LoginCredentials credentials) {
        logger.info("Processing login request for email: {}", credentials.getEmail());
        try {
            AuthResponse loggedUser = authService.login(credentials);
            logger.info("User logged in successfully: {}", credentials.getEmail());
            return ResponseEntity.ok().header("Authorization", "Bearer " + loggedUser.getToken()).body(loggedUser.getUser());
        } catch (Exception e) {
            logger.error("Login failed for email {}: {}", credentials.getEmail(), e.getMessage(), e);
            throw e;
        }
    }
}