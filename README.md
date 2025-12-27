# Spring Boot API Demo

## Overview

This project serves as both a **reusable commons package** and a **demonstration implementation** for building modern Spring Boot REST APIs. It provides a comprehensive set of utilities, patterns, and best practices that can be extracted into a shared library and extended for use in production applications.

The demo implementation showcases a **course enrollment system** with users, courses, categories, and enrollments, demonstrating various Spring Boot capabilities including JPA auditing, GraphQL, JSON Merge-Patch operations, soft deletes, optimistic locking, and MapStruct-based entity-DTO mapping.

## Key Features

- **Reusable Commons Package**: Generic base classes, interfaces, and utilities designed for extraction into a shared library
- **Production-Ready Patterns**: Optimistic locking, soft deletes, audit trails, comprehensive validation, and error handling
- **Modern API Standards**: RESTful API with JSON Merge-Patch (RFC 7396), GraphQL queries, and OpenAPI 3.0 specifications
- **Type-Safe Mapping**: MapStruct-based entity-DTO conversion with bidirectional mapping support
- **Flexible Entity Design**: Support for auto-increment IDs, natural keys, and composite primary keys
- **Extensible Architecture**: Designed with modularity and extensibility in mind for easy adaptation to different domains

## Domain Model

The demo implements a course enrollment system. See the [Entity Relationship Diagram](docs/erd.mmd) for the complete data model.

**Entities:**
- **User**: Students and teachers with soft-delete support
- **Course**: Courses with categories, teachers, and audit tracking
- **Category**: Course categories with natural string keys
- **Enrollment**: Many-to-many relationship between users and courses with composite keys

## Documentation

- **[JPA Entity Architecture](docs/jpa_entities.md)** - Comprehensive guide to entity base classes, optimistic locking, soft deletes, and Lombok integration
- **[Common Mapper Pattern](docs/mappers.md)** - Details on MapStruct-based entity-DTO mapping for bidirectional conversion
- **[Entity Relationship Diagram](docs/erd.mmd)** - Visual representation of the domain model
- **[Quick Test Guide](docs/QUICK-TEST-GUIDE.md)** - Step-by-step testing instructions
- **[Testing Guide](docs/testing-guide.md)** - Comprehensive testing strategies and examples

## Feature Status & Roadmap

### ✅ Data Layer
- [x] **Entity Architecture**
  - [x] Multiple primary key strategies: auto-increment Long, natural String, composite keys
  - [x] Generic base classes ([`AbstractAutoIncrementCommonEntity`](src/main/java/com/example/common/entity/AbstractAutoIncrementCommonEntity.java), [`AbstractCommonEntity<T>`](src/main/java/com/example/common/entity/AbstractCommonEntity.java))
  - [x] Optimistic locking with `@Version` field
  - [x] Soft delete support via [`DeletableEntity`](src/main/java/com/example/common/entity/DeletableEntity.java) interface
  - [x] Enumerations ([`EnrollmentGrade`](src/main/java/com/example/demo/entity/EnrollmentGrade.java))
  - [x] Lazy loading for all relationships (`FetchType.LAZY`)
- [x] **Auditing**
  - [x] Hibernate Envers integration for change tracking
  - [x] Entity-level auditing with `@Audited`
  - [ ] `AuditReader` for querying historical data
  - [ ] Custom revision repository
  - [ ] Spring Data JPA auditing (`@CreatedBy`, `@LastModifiedBy`)
- [x] **Mapping**
  - [x] MapStruct-based entity-DTO conversion
  - [x] Generic [`CommonMapper`](src/main/java/com/example/common/mapper/CommonMapper.java) interface
  - [x] Support for entity-to-DTO, full updates (PUT), and partial updates (PATCH)
  - [x] [`JsonNullable`](src/main/java/com/example/common/mapper/JsonNullableMapper.java) for distinguishing null vs absent fields
- [ ] **Repository Enhancements**
  - [ ] Custom repository implementations using `EntityManager`
  - [ ] Generic filtering, sorting, and pagination utilities
  - [ ] UUID external references for public APIs
  - [ ] SQL initialization scripts
  - [ ] Liquibase schema versioning and migration

### ✅ API Layer
- [x] **RESTful API**
  - [x] JSON Merge-Patch (RFC 7396) for partial updates
  - [x] OpenAPI 3.0 specification
  - [x] Code generation from OpenAPI spec using `openapi-generator`
  - [ ] Problem Details (RFC 9457) for standardized error responses
  - [ ] Generic resource filtering and sorting
  - [ ] Pagination with customizable metadata (response body vs headers)
- [x] **GraphQL**
  - [x] GraphQL schema and resolvers
  - [x] Integration with existing service layer
  - [x] Query support for entities
  - [ ] Mutation support
  - [ ] Subscription support
- [ ] **API Design**
  - [x] Service layer abstraction
  - [ ] Multiple DTO types (query DTOs vs command DTOs)
  - [ ] API-first development with OpenAPI-generated models
  - [ ] Versioning strategy

### 🔄 Security
- [x] **Authentication & Authorization**
  - [x] JWT-based [`SecurityUtils`](src/main/java/com/example/common/util/SecurityUtils.java)
  - [ ] Role-based authorization
  - [ ] Method-level security with `@EnableMethodSecurity`
  - [ ] Proxy user support for delegation
- [ ] **Auditing Integration**
  - [ ] `AuditorAware` implementation for user tracking
  - [ ] Automatic population of created/modified user fields
  - [ ] Role-based PATCH extent (field-level permissions)

### 🧪 Testing
- [x] **Unit Tests**
  - [x] Service layer tests with mocked dependencies
  - [x] Mapper validation tests
- [x] **Integration Tests**
  - [x] Controller tests with `@WebMvcTest`
  - [x] Repository integration tests
  - [x] Testcontainers for database testing
- [ ] **Testing Enhancements**
  - [ ] End-to-end API tests
  - [ ] GraphQL query/mutation tests
  - [ ] Security integration tests
  - [ ] Performance and load testing

### 🏗️ Architecture & Infrastructure
- [ ] **Modularity**
  - [ ] Spring Modulith for code separation and module boundaries
  - [ ] Event-driven communication between modules
  - [ ] Module-level integration tests
- [ ] **Configuration**
  - [ ] Encrypted data source configuration
  - [ ] Environment-specific properties management
  - [ ] Feature flags
- [ ] **Cross-Cutting Concerns**
  - [ ] Global error handler with consistent responses
  - [ ] Application events for decoupled communication
  - [ ] Excel export functionality (`ExcelExportable` interface)
  - [ ] Complex validation (single field, cross-field, group validation)
  - [ ] Move `EntityManager` from mappers to common service utilities

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: H2 (dev), PostgreSQL (production-ready)
- **ORM**: Spring Data JPA, Hibernate
- **API**: Spring MVC (REST), Spring GraphQL
- **Mapping**: MapStruct
- **Validation**: Jakarta Validation (Bean Validation)
- **Auditing**: Hibernate Envers
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Build Tool**: Gradle
- **Documentation**: OpenAPI 3.0, Markdown

## Getting Started

### Prerequisites
- Java 17 or higher
- Gradle 8.x

### Running the Application

```cmd
.\gradlew bootRun
```

The application will start on `http://localhost:8080`.

### Running Tests

```cmd
.\gradlew test
```

### Building the Project

```cmd
.\gradlew clean build
```

## Building a Container Image

This project uses the Gradle Jib plugin to build OCI images and publish them to GitHub Container Registry (GHCR). The target image defaults to `ghcr.io/<GITHUB_REPOSITORY>`, falling back to `ghcr.io/example-org/spring-boot-api-demo` when the `GITHUB_REPOSITORY` environment variable is not set.

### Authenticate to GHCR

```powershell
docker login ghcr.io -u <github-username> -p <github-personal-access-token>
```

The token must include the `write:packages` scope.

### Build and Push (Jib)

```powershell
.\gradlew jib
```

This command builds the image and pushes `latest` plus an additional tag matching the project version.

### Build to Local Docker Daemon (Jib)

```powershell
.\gradlew jibDockerBuild
```

After the image is loaded locally, run it as usual:

```powershell
docker run --rm -p 8080:8080 -e application.profile=dev ghcr.io/example-org/spring-boot-api-demo:latest
```

### Boot Build Image (native)

If you prefer Paketo buildpacks and want a native image (GraalVM), use Spring Boot’s `bootBuildImage`. The project is configured to build and tag `ghcr.io/danielpaval/spring-boot-accelerator:latest` and enable native via `BP_NATIVE_IMAGE=true`.

- Build native image locally (no push):

```powershell
cd C:\Repositories\spring-boot-api-demo; .\gradlew bootBuildImage
```

- Build and publish to GHCR in one step:

```powershell
cd C:\Repositories\spring-boot-api-demo; .\gradlew bootBuildImage --publishImage
```

- Override image name at the CLI (optional):

```powershell
cd C:\Repositories\spring-boot-api-demo; .\gradlew bootBuildImage --publishImage --imageName=ghcr.io/danielpaval/spring-boot-accelerator:0.0.1-SNAPSHOT
```

- Retag and push an already built local image (if you created `docker.io/library/spring-boot-accelerator:0.0.1-SNAPSHOT`):

```powershell
docker tag docker.io/library/spring-boot-accelerator:0.0.1-SNAPSHOT ghcr.io/danielpaval/spring-boot-accelerator:latest; docker push ghcr.io/danielpaval/spring-boot-accelerator:latest
```

Note:
- Ensure you’re authenticated to GHCR (via Docker credential helper or `docker login`).
- Use Jib for JVM images; use `bootBuildImage` for native images. Avoid building both to the same tag concurrently.

## API Endpoints

### REST API
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user (full replacement)
- `PATCH /api/users/{id}` - Partial update user (JSON Merge-Patch)
- `DELETE /api/users/{id}` - Soft delete user

Similar endpoints exist for `/api/courses`, `/api/categories`, and `/api/enrollments`.

### GraphQL
- Endpoint: `/graphql`
- GraphiQL UI: `/graphiql` (when enabled)

### OpenAPI
- Specification: `/v3/api-docs`
- Swagger UI: `/swagger-ui.html`

## References

### JSON Merge-Patch
- https://medium.com/@AlexanderObregon/working-with-json-patch-vs-merge-patch-for-partial-updates-in-spring-boot-apis-57377bfe4e5a
- https://medium.com/@ljcanales/handling-partial-updates-in-spring-boot-a-cleaner-approach-to-patch-requests-6b13ae2a45e0
- https://gaetanopiazzolla.github.io/java/jsonpatch/springboot/2024/09/25/boot-patch.html
- https://www.baeldung.com/javax-validation-method-constraints#bd-3-creating-cross-parameter-constraints

### OpenAPI Spring Generator

- https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin
- https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/spring.md
- https://medium.com/@jugurtha.aitoufella/custom-validation-with-openapigenerator-and-spring-boot-3-34a656e815c8