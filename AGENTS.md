# Repository Guidelines

## Project Structure & Module Organization
- The project is partitioned into a common, reusable package (`src/main/java/com/example/common`) and a demo implementation (`src/main/java/com/example/demo`).
- `src/main/java`: Code under `com.example.*` (controllers, services, repositories, mappers, DTOs).
- `src/main/resources`: `application.yml`, profile overrides, `openapi.yml`, GraphQL schemas.
- `src/test/{java,resources}`: Tests mirroring main packages and test fixtures.
- Root: `build.gradle`, `gradlew*`, `docker-compose.yml`, `api.rest`, `docs/`, `scripts/`.

## Build, Test, and Development Commands
- Build: `./gradlew build` — compile, run tests, package.
- Run (dev): `./gradlew bootRun -Dspring.profiles.active=dev` — API at `http://localhost:8080`.
- Test: `./gradlew test` — JUnit 5; filter with `--tests 'com.example..*'`.
- OpenAPI: `./gradlew openApiGenerate` — regenerate interfaces from `src/main/resources/openapi.yml`.
- Infra: `docker compose up -d` — start local MSSQL/Keycloak as defined.

## Coding Style & Naming Conventions
- Java 21, Spring Boot 3; 4-space indentation; cohesive, small methods.
- Lombok for boilerplate; MapStruct for `*Mapper` implementations.
- Package by feature under `com.example.demo.*`.
- Names: `*Controller`, `*Service`, `*Repository`, `*Dto`, `*Mapper`, `*Specification`.

## Architecture Overview
- Layered: controllers → services → repositories; DTOs mapped via MapStruct.
- Persistence: Spring Data JPA with MSSQL; auditing via Envers.
- API surfaces: REST (Spring MVC), GraphQL, and OpenAPI-generated interfaces.
- Security: OAuth2 Resource Server; local testing assumes Keycloak on `:8180`.

## Testing Guidelines
- Stack: JUnit 5, Spring Boot Test, Testcontainers (MSSQL).
- Structure: tests mirror source packages; class names end with `*Test`.
- Run: `./gradlew test`; prefer slice tests where valuable; add integration tests for repositories/services.

## Commit & Pull Request Guidelines
- Conventional Commits: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `chore:` (e.g., `feat: add course pagination`).
- Branches: `feature/<short-desc>` or `fix/<short-desc>`.
- PR checklist: description with rationale, linked issues, tests updated/added, screenshots or logs for API flows, config changes documented, `./gradlew build` passing.

## Security & Configuration Tips
- Never commit secrets; use `.env` and `application-*.yml` for local overrides; start from `.env.example`.
