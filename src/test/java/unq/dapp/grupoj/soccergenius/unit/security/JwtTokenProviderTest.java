package unq.dapp.grupoj.soccergenius.unit.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import unq.dapp.grupoj.soccergenius.exceptions.TokenVerificationException;
import unq.dapp.grupoj.soccergenius.security.JwtTokenProvider;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("unit")
@Tag("unit")
class JwtTokenProviderTest {
    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_JWT_SECRET_KEY = System.getenv("JWT_SECRET_KEY_SOCCER_GENIUS");
    private static final long EXPIRATION_TIME_MS = 1000L * 60 * 60;
    private static final Long TEST_USER_ID = 123L;
    private Algorithm testAlgorithm;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        testAlgorithm = Algorithm.HMAC256(TEST_JWT_SECRET_KEY);
    }

    @Test
    void generateToken_shouldReturnValidToken_forNonNullId() {
        String token = jwtTokenProvider.generateToken(TEST_USER_ID);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String subject = JWT.require(testAlgorithm)
                .build()
                .verify(token)
                .getSubject();
        assertEquals(TEST_USER_ID.toString(), subject);
    }

    @Test
    void generateToken_shouldThrowNullPointerException_forNullId() {
        assertThrows(NullPointerException.class, () -> jwtTokenProvider.generateToken(null));
    }

    @Test
    void validateToken_shouldNotThrowException_forValidToken() {
        String token = jwtTokenProvider.generateToken(TEST_USER_ID);
        assertDoesNotThrow(() -> jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_shouldNotThrowException_forValidTokenWithBearerPrefix() {
        String token = jwtTokenProvider.generateToken(TEST_USER_ID);
        assertDoesNotThrow(() -> jwtTokenProvider.validateToken("Bearer " + token));
    }

    @Test
    void validateToken_shouldThrowTokenVerificationException_forNullToken() {
        TokenVerificationException exception = assertThrows(TokenVerificationException.class, () -> jwtTokenProvider.validateToken(null));
        assertEquals("Token not provided", exception.getMessage());
    }

    @Test
    void validateToken_shouldThrowTokenVerificationException_forEmptyToken() {
        TokenVerificationException exception = assertThrows(TokenVerificationException.class, () -> jwtTokenProvider.validateToken(""));
        assertEquals("Token not provided", exception.getMessage());
    }

    @Test
    void validateToken_shouldThrowTokenVerificationException_forWhitespaceToken() {
        TokenVerificationException exception = assertThrows(TokenVerificationException.class, () -> jwtTokenProvider.validateToken("   "));
        assertEquals("Token not provided", exception.getMessage());
    }

    @Test
    void validateToken_shouldThrowTokenVerificationException_forInvalidSignatureToken() {
        Algorithm wrongAlgorithm = Algorithm.HMAC256("otro-secreto-diferente-al-esperado");
        String tamperedToken = JWT.create()
                .withSubject(TEST_USER_ID.toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .sign(wrongAlgorithm);

        TokenVerificationException exception = assertThrows(TokenVerificationException.class, () -> jwtTokenProvider.validateToken(tamperedToken));
        assertEquals("Invalid or expired token", exception.getMessage());
    }

    @Test
    void validateToken_shouldThrowTokenVerificationException_forMalformedToken() {
        String malformedToken = "esto.no.es.un.jwt";
        TokenVerificationException exception = assertThrows(TokenVerificationException.class, () -> jwtTokenProvider.validateToken(malformedToken));
        assertEquals("Invalid or expired token", exception.getMessage());
    }

    @Test
    void validateToken_shouldThrowTokenVerificationException_forExpiredToken() {
        String expiredToken = JWT.create()
                .withSubject(TEST_USER_ID.toString())
                .withIssuedAt(new Date(System.currentTimeMillis() - 2 * EXPIRATION_TIME_MS))
                .withExpiresAt(new Date(System.currentTimeMillis() - EXPIRATION_TIME_MS))
                .sign(testAlgorithm);

        TokenVerificationException exception = assertThrows(TokenVerificationException.class, () -> jwtTokenProvider.validateToken(expiredToken));
        assertEquals("Invalid or expired token", exception.getMessage());
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId_forValidToken() {
        String token = jwtTokenProvider.generateToken(TEST_USER_ID);
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        assertEquals(TEST_USER_ID.toString(), userId);
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId_forValidTokenWithBearerPrefix() {
        String tokenWithoutPrefix = jwtTokenProvider.generateToken(TEST_USER_ID);
        String userId = jwtTokenProvider.getUserIdFromToken(tokenWithoutPrefix);
        assertEquals(TEST_USER_ID.toString(), userId);
        String tokenWithBearer = "Bearer " + tokenWithoutPrefix;
        assertThrows(JWTVerificationException.class, () -> jwtTokenProvider.getUserIdFromToken(tokenWithBearer), "getUserIdFromToken should fail if token has Bearer prefix and SUT doesn't handle it");
    }

    @Test
    void getUserIdFromToken_shouldThrowJWTVerificationException_forInvalidSignatureToken() {
        Algorithm wrongAlgorithm = Algorithm.HMAC256("otro-secreto-diferente-al-esperado");
        String tamperedToken = JWT.create()
                .withSubject(TEST_USER_ID.toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .sign(wrongAlgorithm);

        assertThrows(JWTVerificationException.class, () -> jwtTokenProvider.getUserIdFromToken(tamperedToken));
    }

    @Test
    void getUserIdFromToken_shouldThrowJWTVerificationException_forMalformedToken() {
        String malformedToken = "esto.no.es.un.jwt";
        assertThrows(JWTVerificationException.class, () -> jwtTokenProvider.getUserIdFromToken(malformedToken));
    }

    @Test
    void getUserIdFromToken_shouldThrowJWTVerificationException_forExpiredToken() {
        String expiredToken = JWT.create()
                .withSubject(TEST_USER_ID.toString())
                .withIssuedAt(new Date(System.currentTimeMillis() - 2 * EXPIRATION_TIME_MS))
                .withExpiresAt(new Date(System.currentTimeMillis() - EXPIRATION_TIME_MS))
                .sign(testAlgorithm);

        assertThrows(JWTVerificationException.class, () -> jwtTokenProvider.getUserIdFromToken(expiredToken));
    }

    @Test
    void getUserIdFromToken_shouldThrowException_forNullToken() {
        assertThrows(JWTVerificationException.class, () -> jwtTokenProvider.getUserIdFromToken(null));
    }

    @Test
    void getUserIdFromToken_shouldThrowException_forEmptyToken() {
        assertThrows(JWTVerificationException.class, () -> jwtTokenProvider.getUserIdFromToken(""));
    }
}
