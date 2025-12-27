package com.example.demo.entity;

import com.example.demo.security.SecurityUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.RevisionListener;

@Slf4j
@NoArgsConstructor
public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity entity = (CustomRevisionEntity) revisionEntity;
        if (SecurityUtils.isAuthenticated()) {
            try {
                Long userId = SecurityUtils.getUserId();
                entity.setAuthor(userId);
            } catch (Exception e) {
                log.warn("Could not determine the revision author", e);
            }
        } else {
            log.debug("Unauthenticated revision");
        }
    }
}
