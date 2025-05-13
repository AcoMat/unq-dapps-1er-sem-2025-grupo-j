package unq.dapp.grupoj.soccergenius.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unq.dapp.grupoj.soccergenius.exceptions.LoginException;
import unq.dapp.grupoj.soccergenius.exceptions.RegisterException;
import unq.dapp.grupoj.soccergenius.model.dtos.UserDTO;
import unq.dapp.grupoj.soccergenius.model.dtos.auth.AuthResponse;
import unq.dapp.grupoj.soccergenius.model.dtos.auth.LoginCredentials;
import unq.dapp.grupoj.soccergenius.model.dtos.auth.RegisterFormDTO;
import unq.dapp.grupoj.soccergenius.services.auth.AuthService;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API endpoints for user authentication operations")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with the provided registration details and returns a JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or email already in use", 
            content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
            content = @Content)
    })
    public ResponseEntity<UserDTO> register(
            @Parameter(description = "Registration details", required = true)
            @Valid @RequestBody RegisterFormDTO data) {
        logger.info("Processing registration request for email: {}", data.getEmail());
        try {
            AuthResponse newUser = authService.register(data);
            logger.info("User registered successfully: {}", data.getEmail());
            return ResponseEntity.ok().header("Authorization", "Bearer " + newUser.getToken()).body(newUser.getUser());
        } catch (Exception e) {
            throw new RegisterException(e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(
        summary = "Login a user",
        description = "Authenticates a user with the provided credentials and returns a JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User logged in successfully",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid credentials", 
            content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
            content = @Content)
    })
    public ResponseEntity<UserDTO> login(
            @Parameter(description = "Login credentials", required = true)
            @Valid @RequestBody LoginCredentials credentials) {
        logger.info("Processing login request for email: {}", credentials.getEmail());
        try {
            AuthResponse loggedUser = authService.login(credentials);
            logger.info("User logged in successfully: {}", credentials.getEmail());
            return ResponseEntity.ok().header("Authorization", "Bearer " + loggedUser.getToken()).body(loggedUser.getUser());
        } catch (Exception e) {
            throw new LoginException(e.getMessage());
        }
    }
}
