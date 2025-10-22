# Repository Guidelines

## Project Structure & Modules
- `src/main/java`: Application code under `com.example.*` (controllers, services, repositories, mappers, DTOs).
- `src/main/resources`: Config (`application.yml`, profiles), `openapi.yml`, GraphQL schema files.
- `src/test/java`: Unit/integration tests mirroring main packages; fixtures in `src/test/resources`.
- Root: `build.gradle`, `gradlew*` (Gradle), `docker-compose.yml`, `api.rest` (HTTP client), `docs/`, `scripts/`.

## Build, Test, Run
- Build: `./gradlew build` — compiles, runs tests, creates artifact.
- Run: `./gradlew bootRun -Dspring.profiles.active=dev` — starts API on `http://localhost:8080`.
- Tests: `./gradlew test` — executes JUnit 5 suite (incl. Testcontainers when enabled).
- OpenAPI: `./gradlew openApiGenerate` — regenerates stubs from `src/main/resources/openapi.yml` (also run before compile).
- Infra: `docker compose up -d` — starts local dependencies from `docker-compose.yml`.

## Coding Style & Naming
- Java 21, Spring Boot 3; 4-space indentation; meaningful names; small, cohesive methods.
- Use Lombok annotations for boilerplate; MapStruct for `*Mapper` classes.
- Package by feature under `com.example.demo.*`.
- Conventions: `*Controller`, `*Service`, `*Repository`, `*Dto`, `*Mapper`, `*Specification`.

## Testing Guidelines
- Frameworks: JUnit 5, Spring Boot Test, Testcontainers (MSSQL).
- Location: tests mirror source packages; class names end with `*Test`.
- Run all: `./gradlew test`; filter: `./gradlew test --tests 'com.example..*'`.
- Target: cover service, repository, and web layers; prefer slice tests where applicable.

## Commit & PR Guidelines
- Commits: imperative, concise subject (e.g., "Add course pagination"); add body for context.
- Branches: `feature/<short-desc>` or `fix/<short-desc>`.
- PRs: clear description, linked issues, screenshots or logs for API flows, note config changes, and update docs.
- Quality gate: ensure `./gradlew build` passes before requesting review.

## Security & Configuration
- Never commit secrets; use `.env` and `application-*.yml` for local overrides; see `.env.example`.
- Defaults: API on `8080`; Keycloak expected on `8180` for local tests (see `docs/testing-guide.md`). Ensure OIDC settings match your active profile.
