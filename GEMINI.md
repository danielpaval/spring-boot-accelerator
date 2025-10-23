# Gemini Code-writing Context

This document provides context for the Gemini AI to understand the project structure, conventions, and tasks.

## Project Overview

This is a Spring Boot project that serves as a demonstration and a reusable commons package for building modern REST APIs. It showcases a course enrollment system with users, courses, categories, and enrollments.

The project uses the following main technologies:
- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Data JPA** with Hibernate for database interaction.
- **Spring Security** for authentication and authorization, configured as an OAuth2 resource server.
- **GraphQL** for flexible data queries.
- **Microsoft SQL Server** as the database.
- **Gradle** for dependency management and building the project.
- **MapStruct** for mapping between entities and DTOs.
- **Lombok** to reduce boilerplate code.
- **Testcontainers** for integration testing.
- **OpenAPI Generator** to generate API stubs from a specification.

The architecture is designed to be modular and extensible, with a focus on production-ready patterns like optimistic locking, soft deletes, and audit trails.

## Building and Running

### Prerequisites

- Java 21
- Docker

### Running the Application

1.  **Start the database:**
    The project requires a Microsoft SQL Server database. A Docker Compose file is provided for convenience.

    ```bash
    docker-compose up -d
    ```

2.  **Run the application:**
    Use the Gradle wrapper to run the application.

    ```bash
    ./gradlew bootRun
    ```

    The application will be available at `http://localhost:8080`.

### Running Tests

To run the tests, use the following Gradle command:

```bash
./gradlew test
```

## Development Conventions

### Code Style

The project uses the default IntelliJ code style. No specific linter or formatter is enforced in the build process.

### API Development

The project uses an API-first approach. The API is defined in `src/main/resources/openapi.yml`. The OpenAPI Generator is used to generate server-side interfaces and DTOs. When making changes to the API, you should first update the OpenAPI specification and then regenerate the code by running the `openApiGenerate` Gradle task.

### Database Migrations

The project uses Hibernate's `ddl-auto: update` feature to automatically update the database schema. For production environments, a more robust solution like Liquibase or Flyway should be used.

### Commits

Commit messages should be clear and concise, explaining the "why" behind the change, not just the "what".
