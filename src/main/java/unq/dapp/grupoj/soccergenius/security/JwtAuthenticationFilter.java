package unq.dapp.grupoj.soccergenius.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import unq.dapp.grupoj.soccergenius.exceptions.TokenVerificationException;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();

        String token = extractTokenFromRequest(request);

        String jsonType = "application/json;charset=UTF-8";

        if (token != null) {
            try {
                jwtTokenProvider.validateToken(token);
                String userId = jwtTokenProvider.getUserIdFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (TokenExpiredException e) {
                // Handle expired token specifically
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(jsonType);
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Token has expired. Please login again\"}");
                return; // Stop filter chain
            } catch (JWTVerificationException e) {
                // Handle other JWT verification errors
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(jsonType);
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid token\"}");
                return; // Stop filter chain
            } catch (TokenVerificationException e) {
                // Handle custom token verification errors
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(jsonType);
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + e.getMessage() + "\"}");
                return; // Stop filter chain
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}