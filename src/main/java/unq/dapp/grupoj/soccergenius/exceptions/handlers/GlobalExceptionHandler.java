package unq.dapp.grupoj.soccergenius.exceptions.handlers;

import org.apache.hc.client5.http.auth.InvalidCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import unq.dapp.grupoj.soccergenius.exceptions.AlreadyUsedEmail;
import unq.dapp.grupoj.soccergenius.exceptions.TokenVerificationException;
import unq.dapp.grupoj.soccergenius.exceptions.WrongCredentialsException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ConfigDataResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ConfigDataResourceNotFoundException ex) {
        logger.error("Resource not found: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            logger.warn("Validation error on field '{}': {}", fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class})
    public ResponseEntity<String> notRedeableHttp() {
        logger.warn("Invalid request body format received");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid body format");
    }

    @ExceptionHandler(AlreadyUsedEmail.class)
    public ResponseEntity<String> handleGenericRuntime(AlreadyUsedEmail ex) {
        logger.warn("Registration attempt with already used email: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(TokenVerificationException.class)
    public ResponseEntity<String> handleGenericRuntime(TokenVerificationException ex) {
        logger.error("Token verification exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(WrongCredentialsException.class)
    public ResponseEntity<String> handleWrongCredentials(WrongCredentialsException ex) {
        logger.error("Wrong credentials exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

}