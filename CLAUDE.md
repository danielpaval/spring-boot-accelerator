# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.6 application demonstrating a reusable commons package pattern for building REST APIs. The demo implements a course enrollment system showcasing JPA patterns, GraphQL, OAuth2 security, and comprehensive auditing.

**Key Technologies:**
- Java 21 with Spring Boot 3.5.6
- Spring Data JPA with Hibernate Envers for audit trails
- Spring Security with OAuth2 Resource Server (JWT)
- MapStruct for entity-to-DTO mapping
- Spring GraphQL alongside REST
- Microsoft SQL Server
- OpenAPI-driven API design with code generation
- TestContainers for integration testing

## Common Development Commands

### Building and Running

```bash
# Build the project
.\gradlew build

# Run the application
.\gradlew bootRun

# Clean build
.\gradlew clean build

# Generate OpenAPI code (runs automatically before compile)
.\gradlew openApiGenerate
```

### Testing

```bash
# Run all tests
.\gradlew test

# Run tests for a specific class
.\gradlew test --tests "com.example.demo.service.UserServiceTest"

# Run tests with TestContainers (SQL Server)
.\gradlew test
```

### Windows Terminal Notes

- Always use semicolon (;) to separate commands on Windows (not && or &)
- Prefix Gradle wrapper with .\ (e.g., `.\gradlew`)

## High-Level Architecture

### Layer Structure

The application follows a strict layered architecture with generic base classes for reusability:

```
Controller → Service → Repository → Entity
    ↓          ↓          ↓          ↓
   API      Business    Data       Domain
  Layer      Logic     Access      Model
```

**Key Pattern:** All layers use generic type parameters `<ID, ENTITY, DTO, PATCH_DTO>` to minimize duplication.

### Package Organization

**`com.example.common`** - Reusable base classes and utilities (extractable as library):
- `entity/` - `AbstractCommonEntity<T>`, `AbstractAutoIncrementCommonEntity`, `DeletableEntity`
- `service/` - `AbstractCommonService` with CRUD, validation, soft delete support
- `repository/` - `CommonRepository` interface
- `mapper/` - `CommonMapper`, `JsonNullableMapper`
- `security/` - `CommonSecurityUtils` for JWT handling
- `validator/` - Custom validation annotations

**`com.example.demo`** - Application-specific implementation:
- `config/` - Spring configuration (Security, JPA Auditing, Envers, GraphQL, OpenAPI)
- `entity/` - Domain entities (User, Course, Category, Enrollment)
- `dto/` - Transfer objects and criteria
- `mapper/` - MapStruct mappers extending `CommonMapper`
- `repository/` - Repository interfaces extending `CommonRepository`
- `service/` - Service interfaces and implementations extending `AbstractCommonService`
- `controller/` - REST and GraphQL controllers
- `secondary/` - Secondary database context (if using multiple datasources)

### Entity Hierarchy

**Three entity patterns supported:**

1. **Auto-increment Long ID:**
   ```java
   @Entity
   public class User extends AbstractAutoIncrementCommonEntity implements DeletableEntity {
       // Inherits: Long id, Integer version
       // Implements: boolean deleted
   }
   ```

2. **Custom ID type:**
   ```java
   @Entity
   public class Category extends AbstractCommonEntity<String> {
       // ID: String code (manually assigned)
       // Inherits: Integer version
   }
   ```

3. **Composite ID:**
   ```java
   @Entity
   public class Enrollment implements CommonEntity<EnrollmentId> {
       @EmbeddedId
       private EnrollmentId id; // Composite key
   }
   ```

### Service Layer Pattern

All services follow this structure:

```java
public interface UserService extends CommonService<Long, User, UserDto, UserPatchDto> {
    // Custom methods beyond CRUD
}

@Service
public class DefaultUserService
    extends AbstractCommonService<Long, User, UserDto, UserPatchDto>
    implements UserService {
    // AbstractCommonService provides: create, findById, update, patch, deleteById
    // Automatically handles: validation, soft deletes, optimistic locking
}
```

**AbstractCommonService provides:**
- `create(DTO)` - Validates and persists new entity
- `findById(ID)` - Retrieves by ID (respects soft delete)
- `update(ID, DTO)` - Full update with version check
- `patch(ID, PATCH_DTO)` - Partial update using MapStruct
- `deleteById(ID)` - Hard or soft delete (if entity implements `DeletableEntity`)
- Integrated validation and transactional management

### MapStruct Mappers

```java
@Mapper(componentModel = "spring", uses = {JsonNullableMapper.class})
public interface UserMapper extends CommonMapper<Long, User, UserDto, UserPatchDto> {
    // Inherits: toEntity(DTO), toDto(ENTITY), patchEntity(PATCH_DTO, ENTITY)

    @Named("categoryIdToCategory")
    default Category categoryIdToCategory(String categoryId, @Context EntityManager em) {
        return em.getReference(Category.class, categoryId);
    }
}
```

**Key features:**
- Compile-time code generation
- `JsonNullableMapper` handles OpenAPI's nullable wrapper types
- Use `@Context EntityManager` for lazy-loading relationship mapping
- `@Named` methods for custom conversion logic

## Critical Implementation Patterns

### Soft Delete Pattern

Entities implementing `DeletableEntity` use soft deletes:

```java
@Entity
public class User extends AbstractAutoIncrementCommonEntity implements DeletableEntity {
    @Column(name = "deleted", nullable = false)
    @Convert(converter = BooleanZeroOneConverter.class)
    private boolean deleted = false;
}
```

When `deleteById()` is called, `AbstractCommonService` sets `deleted = true` instead of removing the row.

### Audit Trail (Two Systems)

**1. JPA Auditing (Current State):**
```java
@CreatedDate
private OffsetDateTime createdDate;

@CreatedBy
private Long createdBy; // Auto-populated from JWT via SpringSecurityAuditorAware
```

**2. Hibernate Envers (Change History):**
```java
@Audited
@Entity
public class Course extends AbstractAutoIncrementCommonEntity {
    // All changes tracked in audit_courses table
}

// Retrieve history via RevisionRepository:
List<Revision<Long, Course>> revisions = courseRepository.findRevisions(courseId);
```

### OpenAPI Code Generation

**Workflow:**
1. Define API in `src/main/resources/openapi.yml`
2. Run `.\gradlew openApiGenerate` (or just `.\gradlew build`)
3. Generated code appears in `build/generated/openapi/`:
   - Interfaces: `com.example.demo.generated.api`
   - DTOs: `com.example.demo.generated.dto`
4. Controllers implement generated interfaces

**Import mappings** in `build.gradle` allow using custom DTOs instead of generated ones (see lines 100-106).

### Security Architecture

**Authentication:**
- OAuth2 Resource Server with JWT tokens
- Issuer URI: Configured via `JWT_ISSUER_URI` environment variable
- Stateless session (no server-side session storage)

**Authorization:**
- Method-level: `@PreAuthorize("hasRole('ADMIN')")`
- Programmatic: `CommonSecurityUtils.getCurrentUserId()`, `hasRole("ADMIN")`
- Custom: `AuthorizationService` for complex business logic

**Public endpoints:**
- `/graphql` - GraphQL endpoint
- `/v3/api-docs/**` - OpenAPI specification
- `/swagger-ui/**` - Swagger UI

### Dynamic Querying with Specifications

Use JPA Specifications for complex filtering:

```java
public class CourseSpecification {
    public static Specification<Course> hasName(String name) {
        return (root, query, cb) -> cb.like(root.get("name"), "%" + name + "%");
    }
}

// In service:
Specification<Course> spec = CourseSpecification.hasName(criteria.getName());
Page<Course> courses = repository.findAll(spec, pageable);
```

**RSQL support** available via `io.github.perplexhub:rsql-jpa-spring-boot-starter` for dynamic query strings.

## Configuration Notes

### Application Properties

- Custom properties should use `application.` prefix
- Example: `application.jwt.issuer-uri`

### Configuration Classes

- Keep specific annotations in dedicated configuration classes
- `@EnableJpaAuditing` → `JpaAuditingConfiguration`
- `@EnableEnversRepositories` → `EnversAuditingConfiguration`

### Database

**SQL Server configuration:**
- Hibernate dialect: `org.hibernate.dialect.SQLServerDialect`
- Boolean storage: Use `BooleanZeroOneConverter` (stores as 0/1)
- DDL mode: `update` (consider Liquibase for production)

**TestContainers:**
- Tests automatically spin up SQL Server container
- No need for H2 or in-memory databases

## Common Gotchas

1. **Lazy Loading in Mappers:** Use `@Context EntityManager` and `em.getReference()` to load relationships without triggering N+1 queries.

2. **Version Conflicts:** All entities have `@Version` for optimistic locking. Update operations may throw `OptimisticLockingFailureException` if concurrent modification occurs.

3. **Soft Delete Awareness:** `findById()` in `AbstractCommonService` checks `deleted` flag. Deleted entities return 404.

4. **MapStruct + Lombok:** Annotation processor order matters. Lombok must process before MapStruct (configured in `build.gradle`).

5. **OpenAPI Nullable:** Use `JsonNullable<T>` for optional PATCH fields. Distinguish between "field not provided" vs "field set to null".

6. **JWT Roles:** Roles must be extracted from JWT claims. Configure `JwtRolesGrantedAuthoritiesConverter` to map claims to Spring Security authorities.

## Domain Model Summary

**Entities:**
- **User** - Auto-increment ID, soft delete enabled, audited
- **Course** - Auto-increment ID, belongs to Category, tracks creator/updater, audited
- **Category** - String code as ID (e.g., "TECH", "BIZ")
- **Enrollment** - Composite key (userId + courseId), links User to Course with grade and date

**Relationships:**
- User creates/updates Courses (many-to-one)
- Category contains Courses (one-to-many)
- User enrolls in Courses (many-to-many via Enrollment join entity)

## Documentation and API Exploration

- **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **GraphQL Playground:** `http://localhost:8080/graphql` (use GraphQL client)
- **Actuator:** `http://localhost:8080/actuator`

## References

Key documents in `docs/`:
- `jpa_entities.md` - Entity patterns and examples
- `mappers.md` - Mapper implementation guidance (if exists)

External references in README.md:
- JSON Merge-Patch (RFC 7396)
- OpenAPI Generator documentation
