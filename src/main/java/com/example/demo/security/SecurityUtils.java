package com.example.demo.security;

import com.example.common.security.CommonSecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils extends CommonSecurityUtils {

    public static final String ROLES_CLAIM_PATH = "realm_access.roles";

    public static final String USER_ID_CLAIM_NAME = "user_id";

    public static final String ADMIN_ROLE = "ADMIN";

    public static Long getUserId() {
        return getJwtClaim(USER_ID_CLAIM_NAME, Long.class);
    }

    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return isAuthenticated(authentication) && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(ROLE_PREFIX + ADMIN_ROLE));
    }

}
