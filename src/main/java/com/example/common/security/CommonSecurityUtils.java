package com.example.common.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;

public class CommonSecurityUtils {

    public static final String ROLE_PREFIX = "ROLE_";

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static boolean isAuthenticated(Authentication authentication) {
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return isAuthenticated(authentication);
    }

    public static boolean isAuthorized(List<String> authorities) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            return false;
        }
        if (authorities.isEmpty()) {
            return true;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authorities::contains);
    }

    public static Jwt getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken && isAuthenticated(authentication)) {
            return jwtAuthenticationToken.getToken();
        }
        return null;
    }

    /**
     * Extracts a claim value from the current user's JWT using a dot-separated path notation.
     * Supports navigating through nested maps in the JWT claims.
     *
     * @param claimPath the dot-separated path to the claim (e.g., "realm_access.roles" or "user_id")
     * @param type the expected type of the claim value
     * @param <T> the type parameter
     * @return the claim value cast to the specified type, or null if not found or wrong type
     */
    @SuppressWarnings("unchecked")
    public static <T> T getJwtClaim(String claimPath, Class<T> type) {
        Jwt jwt = getJwt();
        if (jwt == null || claimPath == null || claimPath.isEmpty()) {
            return null;
        }

        String[] parts = claimPath.split("\\.");
        Object current = null;

        // Navigate through the path
        for (int i = 0; i < parts.length; i++) {
            if (i == 0) {
                current = jwt.getClaim(parts[i]);
            } else {
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(parts[i]);
                } else {
                    return null;
                }
            }

            if (current == null) {
                return null;
            }
        }

        // Cast to the requested type if possible
        if (type.isInstance(current)) {
            return type.cast(current);
        }

        // Handle string-to-number conversions
        if (current instanceof String) {
            try {
                if (type == Long.class) {
                    return type.cast(Long.valueOf((String) current));
                }
                if (type == Integer.class) {
                    return type.cast(Integer.valueOf((String) current));
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Extracts a claim value from the current user's JWT as a String using a dot-separated path notation.
     *
     * @param claimPath the dot-separated path to the claim (e.g., "realm_access.roles" or "user_id")
     * @return the claim value as a String, or null if not found
     */
    public static String getJwtClaim(String claimPath) {
        return getJwtClaim(claimPath, String.class);
    }

}
