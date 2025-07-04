package unq.dapp.grupoj.soccergenius.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import unq.dapp.grupoj.soccergenius.exceptions.TokenVerificationException;

import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final long EXPIRATION_TIME = 1000L * 60 * 60; // 1 hora
    private final Algorithm algorithm = Algorithm.HMAC256(System.getenv("JWT_SECRET_KEY_SOCCER_GENIUS"));


    public String generateToken(Long id) {
        return JWT.create()
                .withSubject(id.toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    public void validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                throw new TokenVerificationException("Token not provided");
            }

            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new TokenVerificationException("Invalid or expired token");
        }
    }

    public String getUserIdFromToken(String token) {
        return JWT.require(algorithm)
                .build()
                .verify(token)
                .getSubject();
    }
}
