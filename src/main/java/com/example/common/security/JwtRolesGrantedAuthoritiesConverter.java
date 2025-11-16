package com.example.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JwtRolesGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String rolesClaimPath;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<String> roles = extractClaimFromPath(jwt, rolesClaimPath);

        if (roles == null) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(CommonSecurityUtils.ROLE_PREFIX + role))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Collection<String> extractClaimFromPath(Jwt jwt, String path) {
        if (jwt == null || path == null || path.isEmpty()) {
            return null;
        }

        String[] parts = path.split("\\.");
        Object current = null;

        // Navigate through the path
        for (int i = 0; i < parts.length; i++) {
            if (i == 0) {
                current = jwt.getClaim(parts[i]);
            } else {
                if (current instanceof java.util.Map) {
                    current = ((java.util.Map<String, Object>) current).get(parts[i]);
                } else {
                    return null;
                }
            }

            if (current == null) {
                return null;
            }
        }

        // The final value should be a collection of strings
        if (current instanceof Collection) {
            return (Collection<String>) current;
        }

        return null;
    }


}
