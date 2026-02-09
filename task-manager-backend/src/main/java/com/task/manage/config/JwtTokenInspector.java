package com.task.manage.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Utility class to help debug and inspect JWT tokens from Keycloak.
 * This is useful for troubleshooting authentication and auditing issues.
 */
@Component
@Slf4j
public class JwtTokenInspector {

    /**
     * Logs all claims present in the current JWT token.
     * Call this method in your controllers or services to debug JWT token content.
     */
    public void logAllTokenClaims() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.warn("No authentication found in SecurityContext");
            return;
        }

        log.info("Authentication type: {}", authentication.getClass().getSimpleName());
        log.info("Authentication principal: {}", authentication.getPrincipal().getClass().getSimpleName());
        log.info("Authentication name: {}", authentication.getName());

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            log.warn("Authentication is not JwtAuthenticationToken, cannot inspect JWT claims");
            return;
        }

        Jwt jwt = jwtAuth.getToken();

        log.info("=== JWT Token Details ===");
        log.info("Token ID (jti): {}", jwt.getId());
        log.info("Issuer (iss): {}", jwt.getIssuer());
        log.info("Subject (sub): {}", jwt.getSubject());
        log.info("Issued At: {}", jwt.getIssuedAt());
        log.info("Expires At: {}", jwt.getExpiresAt());

        log.info("=== All JWT Claims ===");
        Map<String, Object> claims = jwt.getClaims();
        claims.forEach((key, value) -> log.info("  {}: {} (type: {})", key, value, value != null ? value.getClass().getSimpleName() : "null"));

        log.info("=== Commonly Used Claims for Auditing ===");
        log.info("  preferred_username: {}", jwt.getClaimAsString("preferred_username"));
        log.info("  name: {}", jwt.getClaimAsString("name"));
        log.info("  email: {}", jwt.getClaimAsString("email"));
        log.info("  given_name: {}", jwt.getClaimAsString("given_name"));
        log.info("  family_name: {}", jwt.getClaimAsString("family_name"));
    }

    /**
     * Gets the username from the JWT token using the same strategy as KeycloakAuditorAware.
     * This helps verify what username will be used for auditing.
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return "UNKNOWN";
        }

        Jwt jwt = jwtAuth.getToken();

        // Try the same strategies as KeycloakAuditorAware
        String username = jwt.getClaimAsString("preferred_username");
        if (username != null && !username.isBlank()) {
            return username;
        }

        username = jwt.getClaimAsString("name");
        if (username != null && !username.isBlank()) {
            return username;
        }

        username = jwt.getClaimAsString("email");
        if (username != null && !username.isBlank()) {
            return username;
        }

        username = jwt.getSubject();
        if (username != null && !username.isBlank()) {
            return username;
        }

        return authentication.getName();
    }

    /**
     * Checks if a specific claim exists in the current JWT token.
     */
    public boolean hasClaim(String claimName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return false;
        }

        return jwtAuth.getToken().hasClaim(claimName);
    }

    /**
     * Gets a specific claim value from the current JWT token.
     */
    public Object getClaim(String claimName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return null;
        }

        return jwtAuth.getToken().getClaim(claimName);
    }
}
