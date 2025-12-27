package com.example.demo.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for accessing Envers audit/revision data programmatically.
 * This approach is compatible with Spring Native/AOT compilation.
 */
@Service
@Transactional(readOnly = true)
public class EnversRevisionService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Find all revisions for an entity with pagination.
     *
     * @param entityClass the entity class
     * @param id the entity ID
     * @param pageable pagination information
     * @param <T> entity type
     * @param <ID> ID type
     * @return page of revisions
     */
    public <T, ID> Page<Revision<Integer, T>> findRevisions(Class<T> entityClass, ID id, Pageable pageable) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        // Get all revision numbers for this entity
        List<Number> revisionNumbers = auditReader.getRevisions(entityClass, id);

        // Calculate pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), revisionNumbers.size());

        if (start >= revisionNumbers.size()) {
            return Page.empty(pageable);
        }

        // Get the revisions for the current page
        List<Revision<Integer, T>> revisions = revisionNumbers.subList(start, end).stream()
                .map(revNum -> {
                    T entity = auditReader.find(entityClass, id, revNum);
                    DefaultRevisionEntity revisionEntity = auditReader.findRevision(DefaultRevisionEntity.class, revNum);
                    return createRevision(entity, revisionEntity);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(revisions, pageable, revisionNumbers.size());
    }

    /**
     * Find a specific revision for an entity.
     *
     * @param entityClass the entity class
     * @param id the entity ID
     * @param revisionNumber the revision number
     * @param <T> entity type
     * @param <ID> ID type
     * @return optional containing the revision, or empty if not found
     */
    public <T, ID> Optional<Revision<Integer, T>> findRevision(Class<T> entityClass, ID id, Integer revisionNumber) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        try {
            T entity = auditReader.find(entityClass, id, revisionNumber);
            if (entity == null) {
                return Optional.empty();
            }

            DefaultRevisionEntity revisionEntity = auditReader.findRevision(DefaultRevisionEntity.class, revisionNumber);
            return Optional.of(createRevision(entity, revisionEntity));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Find the last change revision for an entity.
     *
     * @param entityClass the entity class
     * @param id the entity ID
     * @param <T> entity type
     * @param <ID> ID type
     * @return optional containing the last revision, or empty if entity not found or not audited
     */
    public <T, ID> Optional<Revision<Integer, T>> findLastChangeRevision(Class<T> entityClass, ID id) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        // Get all revision numbers for this entity
        List<Number> revisionNumbers = auditReader.getRevisions(entityClass, id);

        if (revisionNumbers.isEmpty()) {
            return Optional.empty();
        }

        // Get the last revision
        Number lastRevisionNumber = revisionNumbers.get(revisionNumbers.size() - 1);
        T entity = auditReader.find(entityClass, id, lastRevisionNumber);
        DefaultRevisionEntity revisionEntity = auditReader.findRevision(DefaultRevisionEntity.class, lastRevisionNumber);

        return Optional.of(createRevision(entity, revisionEntity));
    }

    /**
     * Create a Revision object from entity and revision metadata.
     */
    private <T> Revision<Integer, T> createRevision(T entity, DefaultRevisionEntity revisionEntity) {
        RevisionMetadata<Integer> metadata = new RevisionMetadata<>() {
            @Override
            public Optional<Integer> getRevisionNumber() {
                return Optional.of(revisionEntity.getId());
            }

            @Override
            public Optional<Instant> getRevisionInstant() {
                return Optional.of(Instant.ofEpochMilli(revisionEntity.getTimestamp()));
            }

            @Override
            public <T1> T1 getDelegate() {
                return (T1) revisionEntity;
            }
        };

        return Revision.of(metadata, entity);
    }
}
