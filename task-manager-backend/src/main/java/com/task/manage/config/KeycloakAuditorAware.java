package com.task.manage.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class KeycloakAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.debug("Getting current auditor. Authentication: {}",
                authentication != null ? authentication.getClass().getSimpleName() : "null");

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            log.debug("No authenticated user found, returning SYSTEM");
            return Optional.of("SYSTEM");
        }

        // Check if the authentication is a JwtAuthenticationToken
        if (!(authentication instanceof JwtAuthenticationToken)) {
            log.warn("Authentication is not JwtAuthenticationToken, type: {}. Returning authentication name: {}",
                    authentication.getClass().getName(), authentication.getName());
            return Optional.ofNullable(authentication.getName());
        }

        JwtAuthenticationToken jwtAuthentication = (JwtAuthenticationToken) authentication;

        // Log all available claims to help debug
        log.debug("JWT Token Claims: {}", jwtAuthentication.getToken().getClaims());

        // Try multiple strategies to get the username
        String userId = null;

        // Strategy 1: Try preferred_username (most common in Keycloak)
        userId = jwtAuthentication.getToken().getClaimAsString("preferred_username");
        if (userId != null && !userId.isBlank()) {
            log.debug("Found username from 'preferred_username' claim: {}", userId);
            return Optional.of(userId);
        }

        // Strategy 2: Try 'name' claim
        userId = jwtAuthentication.getToken().getClaimAsString("name");
        if (userId != null && !userId.isBlank()) {
            log.debug("Found username from 'name' claim: {}", userId);
            return Optional.of(userId);
        }

        // Strategy 3: Try 'email' claim
        userId = jwtAuthentication.getToken().getClaimAsString("email");
        if (userId != null && !userId.isBlank()) {
            log.debug("Found username from 'email' claim: {}", userId);
            return Optional.of(userId);
        }

        // Strategy 4: Try 'sub' (subject - usually a UUID)
        userId = jwtAuthentication.getToken().getSubject();
        if (userId != null && !userId.isBlank()) {
            log.debug("Found username from 'sub' (subject) claim: {}", userId);
            return Optional.of(userId);
        }

        // Strategy 5: Try extracting from 'jti' (JWT ID) - format might be "username:uuid"
        String jti = jwtAuthentication.getToken().getClaimAsString("jti");
        if (jti != null && jti.contains(":")) {
            // Extract username part before the colon
            userId = jti.substring(0, jti.indexOf(":"));
            if (!userId.isBlank()) {
                log.debug("Found username from 'jti' claim (extracted from '{}'): {}", jti, userId);
                return Optional.of(userId);
            }
        }

        // Strategy 6: Try 'azp' (authorized party - client ID)
        userId = jwtAuthentication.getToken().getClaimAsString("azp");
        if (userId != null && !userId.isBlank()) {
            log.debug("Found username from 'azp' (client ID) claim: {}", userId);
            return Optional.of(userId);
        }

        // Strategy 7: Fallback to authentication name
        userId = authentication.getName();
        if (userId != null && !userId.isBlank()) {
            log.warn("All standard JWT claims were null/empty, falling back to authentication.getName(): {}", userId);
            return Optional.of(userId);
        }

        // Strategy 8: Last resort - use "UNKNOWN_USER"
        log.error("Unable to extract username from JWT token. All claims: {}", jwtAuthentication.getToken().getClaims());
        return Optional.of("UNKNOWN_USER");
    }
}
