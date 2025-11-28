package com.example.common.service;

import com.example.common.dto.CommonDto;
import com.example.common.entity.CommonEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Example usage of {@link CommonService} with a Category domain:
 *
 * <pre>{@code
 * public interface CategoryService extends CommonService<String, Category, CategoryDto, Void> { }
 * }</pre>
 *
 * A concrete implementation would typically extend {@link AbstractCommonService}
 * supplying repository, mapper, and validator via constructor injection.
 *
 * Generic parameters meaning:
 *  ID         -> Identifier type (e.g., String, Long, UUID)
 *  ENTITY     -> JPA entity extending CommonEntity<ID>
 *  DTO        -> Data Transfer Object extending CommonDto<ID>
 *  PATCH_DTO  -> DTO for partial updates (use {@code Void} when patch not supported)
 *
 * Typical create flow:
 * <pre>{@code
 * CategoryDto dto = new CategoryDto();
 * dto.setName("Programming");
 * CategoryDto saved = categoryService.save(dto);
 * }</pre>
 *
 * Query with a specification:
 * <pre>{@code
 * Specification<Category> spec = (root, query, cb) -> cb.like(root.get("name"), "%Prog%");
 * Page<CategoryDto> page = categoryService.findBySpecification(spec, PageRequest.of(0, 20));
 * }</pre>
 *
 * Counting:
 * <pre>{@code
 * long totalMatching = categoryService.countBySpecification(spec);
 * }</pre>
 *
 * Partial update (when PATCH_DTO != Void):
 * <pre>{@code
 * CategoryPatchDto patch = new CategoryPatchDto();
 * patch.setDescription("Updated description");
 * CategoryDto afterPatch = categoryService.patch(categoryId, patch);
 * }</pre>
 *
 * Soft delete semantics:
 * If ENTITY implements {@code DeletableEntity}, {@link #deleteById(Object)} will mark {@code deleted=true}
 * instead of performing a physical removal.
 */
public interface CommonService<ID extends Serializable, ENTITY extends CommonEntity<ID>, DTO extends CommonDto<ID>, PATCH_DTO> {

    DTO save(DTO dto);

    DTO getById(ID id);

    Optional<DTO> findById(ID id);

    Page<DTO> findBySpecification(Specification<ENTITY> specification, Pageable pageable);

    @Transactional(readOnly = true)
    default List<DTO> findAllBySpecification(Specification<ENTITY> specification) {
        return findBySpecification(specification, Pageable.unpaged()).getContent();
    }

    Long countBySpecification(Specification<ENTITY> specification);

    DTO update(ID id, DTO dto);

    DTO patch(ID id, PATCH_DTO dto);

    void deleteById(ID id);

}
