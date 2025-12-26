package com.example.demo.security;

import com.example.demo.config.SecurityProperties;
import com.example.demo.service.UserService;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Spring-managed context holder for SecurityUtils.
 * This allows SecurityUtils to remain a pure static utility class
 * while still accessing Spring-managed beans.
 */
@Component
@Getter
public class SecurityUtilsContext {

    private final SecurityProperties securityProperties;

    private final UserService userService;

    public SecurityUtilsContext(SecurityProperties securityProperties, UserService userService) {
        this.securityProperties = securityProperties;
        this.userService = userService;
        // Initialize the static SecurityUtils with this context
        SecurityUtils.setContext(this);
    }

}
