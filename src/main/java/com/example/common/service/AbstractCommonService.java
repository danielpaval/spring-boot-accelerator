package com.example.common.service;

import com.example.common.dto.CommonDto;
import com.example.common.entity.CommonEntity;
import com.example.common.entity.DeletableEntity;
import com.example.common.mapper.CommonMapper;
import com.example.common.repository.CommonRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * Example implementation using this abstract service for a Category domain:
 *
 * <pre>{@code
 * @Service
 * @Transactional
 * public class DefaultCategoryService extends AbstractCommonService<String, Category, CategoryDto, Void> implements CategoryService {
 *
 *     public DefaultCategoryService(CategoryRepository repository, CategoryMapper mapper, Validator validator) {
 *         super(repository, mapper, validator);
 *     }
 * }
 * }</pre>
 *
 * Where:
 *  - {@code Category} extends {@code CommonEntity<String>}
 *  - {@code CategoryDto} extends {@code CommonDto<String>}
 *  - {@code CategoryRepository} extends {@code CommonRepository<String, Category>}
 *  - {@code CategoryMapper} implements {@code CommonMapper<String, Category, CategoryDto, Void>}
 *
 * Notes:
 *  - The fourth generic parameter ({@code PATCH_DTO}) is {@code Void} here because patch operations are not supported for Category.
 *  - For a patchable entity, replace {@code Void} with a Patch DTO type and implement {@code mapper.patch(patchDto, entity)}.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCommonService<ID extends Serializable, ENTITY extends CommonEntity<ID>, DTO extends CommonDto<ID>, PATCH_DTO> implements CommonService<ID, ENTITY, DTO, PATCH_DTO> {

    protected final CommonRepository<ID, ENTITY> repository;

    protected final CommonMapper<ID, ENTITY, DTO, PATCH_DTO> mapper;

    protected final Validator validator;

    @Override
    @Transactional
    @SneakyThrows
    public DTO save(DTO dto) {
        Set<ConstraintViolation<DTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        ENTITY entity = mapper.getEntityClass().getDeclaredConstructor().newInstance();
        mapper.update(dto, entity);
        if (entity instanceof DeletableEntity deletableEntity) {
            deletableEntity.setDeleted(false);
        }
        entity = repository.save(entity);
        return mapper.map(entity);
    }

    @Override
    public DTO getById(ID id) {
        ENTITY entity = repository.getReferenceById(id);
        return mapper.map(entity);
    }

    @Override
    public Optional<DTO> findById(ID id) {
        return repository.findById(id).map(mapper::map);
    }

    @Override
    public Page<DTO> findBySpecification(Specification<ENTITY> specification, Pageable pageable) {
        return repository
                .findAll(specification, pageable)
                .map(mapper::map);
    }

    @Override
    public Long countBySpecification(Specification<ENTITY> specification) {
        return repository.count(specification);
    }

    @Override
    @Transactional
    public DTO update(ID id, DTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("ID for update cannot be null.");
        }
        Set<ConstraintViolation<DTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        ENTITY entity = repository.getReferenceById(id);
        if (entity instanceof DeletableEntity deletableEntity && deletableEntity.isDeleted()) {
            throw new IllegalStateException("Cannot update a deleted entity with ID: " + id);
        }
        dto.setId(id);
        mapper.update(dto, entity);
        if (entity instanceof DeletableEntity deletableEntity) {
            deletableEntity.setDeleted(false);
        }
        ENTITY updatedEntity = repository.save(entity);
        return mapper.map(updatedEntity);
    }

    @Override
    public DTO patch(ID id, PATCH_DTO patchDto) {
        ENTITY entity = repository.getReferenceById(id);
        if (entity instanceof DeletableEntity deletableEntity && deletableEntity.isDeleted()) {
            throw new IllegalStateException("Cannot update a deleted entity with ID: " + id);
        }
        mapper.patch(patchDto, entity);
        DTO dto = mapper.map(entity);
        Set<ConstraintViolation<DTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        if (entity instanceof DeletableEntity deletableEntity) {
            deletableEntity.setDeleted(false);
        }
        ENTITY updatedEntity = repository.save(entity);
        return mapper.map(updatedEntity);
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        ENTITY entity = repository.getReferenceById(id);
        if (entity instanceof DeletableEntity deletableEntity) {
            deletableEntity.setDeleted(true);
            repository.save(entity);
        } else {
            repository.deleteById(id);
        }
    }

}
