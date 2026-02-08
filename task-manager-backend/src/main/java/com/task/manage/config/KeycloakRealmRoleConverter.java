package com.task.manage.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // 1. Standard scopes
        var defaultConverter = new JwtGrantedAuthoritiesConverter();
        var authorities = defaultConverter.convert(jwt); // Returns collection, need to be mutable

        // 2. Extract Realm Roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && !realmAccess.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");

            if (roles != null) {
                // Map roles exactly as they are (no "ROLE_" prefix, no uppercase)
                var roleAuthorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // Combine sets
                // Note: We need a mutable collection to combine them effectively if 'authorities' is immutable
                return Stream.concat(authorities.stream(), roleAuthorities.stream())
                        .collect(Collectors.toSet());
            }
        }
        return authorities;
    }
}
