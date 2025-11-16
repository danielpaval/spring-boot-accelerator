package com.example.demo.config;

import com.example.common.security.JwtRolesGrantedAuthoritiesConverter;
import com.example.demo.security.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfiguration {

    @Bean
    public JwtRolesGrantedAuthoritiesConverter jwtRolesGrantedAuthoritiesConverter() {
        return new JwtRolesGrantedAuthoritiesConverter(SecurityUtils.ROLES_CLAIM_PATH);
    }

}

