package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.security.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;

import java.util.Optional;

public class SpringSecurityAuditorAware implements AuditorAware<User> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @NonNull
    public Optional<User> getCurrentAuditor() {
        try {
            Long userId = SecurityUtils.getUserId();
            return Optional.of(entityManager.getReference(User.class, userId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
