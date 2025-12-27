package com.example.demo.security;

import com.example.common.security.CommonSecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils extends CommonSecurityUtils {

    public static final String ADMIN_ROLE = "ADMIN";

    private static SecurityUtilsContext context;

    // Package-private for initialization by SecurityUtilsContext
    static void setContext(SecurityUtilsContext ctx) {
        context = ctx;
    }

    private static SecurityUtilsContext getContext() {
        if (context == null) {
            throw new IllegalStateException("SecurityUtils not initialized. Ensure Spring context is loaded.");
        }
        return context;
    }

    public static String getExternalUserId() {
        return getJwtClaim(getContext().getSecurityProperties().getExternalUserIdClaimPath(), String.class);
    }

    public static Long getUserId() {
        String externalUserId = getExternalUserId();
        return getContext().getUserService().findIdByExternalId(externalUserId)
                .orElseThrow(() -> new IllegalStateException("User not found for external ID: " + externalUserId));
    }

    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return isAuthenticated(authentication) && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(ROLE_PREFIX + ADMIN_ROLE));
    }

}
