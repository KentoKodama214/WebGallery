# CLAUDE.md - AI Assistant Guide for WebGallary

## Project Overview

WebGallary is a photo gallery web application built with Spring Boot. Users can register accounts, upload photos with metadata/EXIF data, browse photo galleries, tag photos, and manage favorites. The codebase and all documentation/comments are in Japanese.

## Tech Stack

| Component       | Technology                        |
|-----------------|-----------------------------------|
| Language        | Java 21                           |
| Build Tool      | Gradle 8.7 (wrapper included)     |
| Framework       | Spring Boot 3.3.3                 |
| Security        | Spring Security 3.3.3 (BCrypt)    |
| Template Engine | Thymeleaf 3.3.3                   |
| ORM             | MyBatis 3.0.3                     |
| Database        | PostgreSQL (driver 42.7.4)        |
| Code Generation | Lombok 1.18.34                    |
| Object Mapping  | ModelMapper 3.2.1                 |
| Testing         | JUnit Jupiter 5.11.1, Mockito 5.14|
| Packaging       | WAR (Tomcat deployment)           |

## Build & Run Commands

```bash
# Build the project
./backend/gradlew -p backend build

# Run tests only
./backend/gradlew -p backend test

# Run the application
./backend/gradlew -p backend bootRun

# Build WAR file
./backend/gradlew -p backend war

# Clean build
./backend/gradlew -p backend clean build

# Start PostgreSQL database (required before running app)
docker-compose up -d
```

## Project Structure

```
WebGallary/
├── docker-compose.yml              # PostgreSQL container setup
├── docker/db/                      # Dockerfile for DB image
├── db/                             # Database initialization scripts
│   ├── init/init-db.sh             # DB init entrypoint
│   ├── common/                     # Common schema SQL (account, kbn_mst, location_mst)
│   └── photo/                      # Photo schema SQL (photo_mst, photo_tag_mst, photo_favorite)
├── scripts/                        # CI/CD scripts
│   └── check-architecture.sh       # Architecture violation checker
├── frontend/                       # Next.js frontend (React)
├── backend/                        # Spring Boot backend
│   ├── build.gradle                # Gradle build config
│   ├── settings.gradle             # Gradle settings
│   ├── gradlew / gradlew.bat      # Gradle wrapper scripts
│   ├── gradle/                     # Gradle wrapper JAR
│   ├── config/checkstyle/          # Checkstyle config
│   ├── set-env.sh                  # Environment variable setup script
│   └── src/
│       ├── main/
│       │   ├── java/com/web/gallary/
│       │   │   ├── WebGallaryApplication.java   # Boot main class
│       │   │   ├── ServletInitializer.java      # WAR deployment initializer
│       │   │   ├── AccountPrincipal.java        # Spring Security UserDetails
│       │   │   ├── config/                      # Configuration classes
│       │   │   ├── constant/                    # Constants (ApiRoutes, Consts, MessageConst)
│       │   │   ├── controller/                  # MVC + REST controllers
│       │   │   │   ├── request/                 # Request DTOs
│       │   │   │   └── response/                # Response DTOs
│       │   │   ├── entity/                      # Database entities
│       │   │   ├── enumuration/                 # Enums (note: package typo is intentional)
│       │   │   ├── exception/                   # Custom exception classes
│       │   │   ├── helper/                      # Helper utilities (Session, Kbn)
│       │   │   ├── mapper/                      # MyBatis mapper interfaces
│       │   │   ├── model/                       # Transfer/business model objects
│       │   │   ├── repository/                  # Repository interfaces
│       │   │   │   └── impl/                    # Repository implementations
│       │   │   ├── service/                     # Service interfaces
│       │   │   │   └── impl/                    # Service implementations
│       │   │   ├── type_handler/                # MyBatis enum type handlers
│       │   │   └── util/                        # URL utility classes
│       │   └── resources/
│       │       ├── application.yml              # App configuration
│       │       ├── messages.properties          # Message strings
│       │       └── com/web/gallary/mapper/      # MyBatis XML mapper files
│       └── test/
│           ├── java/com/web/gallary/            # Test classes (mirrors main structure)
│           │   ├── controller/
│           │   │   └── integration/             # Controller integration tests
│           │   ├── mapper/                      # Mapper unit tests
│           │   ├── repository/impl/
│           │   │   └── integration/             # Repository integration tests
│           │   ├── service/impl/
│           │   │   └── integration/             # Service integration tests
│           │   ├── helper/                      # Helper unit tests
│           │   └── util/                        # Utility unit tests
│           └── resources/
│               ├── application-test.yml         # Test configuration
│               └── sql/                         # Test SQL fixtures
│                   ├── common/                  # Shared test data
│                   ├── controller/              # Controller test data
│                   ├── mapper/                  # Mapper test data
│                   ├── repository/              # Repository test data
│                   └── service/                 # Service test data
```

## Architecture

### Layered Architecture (Controller -> Service -> Repository -> Mapper)

1. **Controller Layer** (`controller/`)
   - MVC controllers return Thymeleaf view names
   - REST controllers (suffix `RestController`) return JSON responses
   - Exception handling via `CommonControllerAdvice` (MVC) and `CommonRestControllerAdvice` (REST)
   - Request validation uses `@Valid` with request DTOs in `controller/request/`
   - All API routes defined centrally in `constant/ApiRoutes.java`

2. **Service Layer** (`service/` + `service/impl/`)
   - Interface-based design: interface in `service/`, implementation in `service/impl/`
   - Annotated with `@Service` and `@Transactional` where needed
   - Business logic and validation lives here

3. **Repository Layer** (`repository/` + `repository/impl/`)
   - Interface-based design: interface in `repository/`, implementation in `repository/impl/`
   - Annotated with `@Repository`
   - Delegates to MyBatis mappers for database access
   - `FileRepository` handles file system operations

4. **MyBatis Mapper Layer** (`mapper/`)
   - Java interfaces define method signatures
   - SQL defined in XML files at `resources/com/web/gallary/mapper/*.xml`
   - Custom type handlers in `type_handler/` for enum-to-DB conversion

### Key Patterns

- **Interface + Impl**: Services and repositories always have an interface and a separate `impl/` implementation
- **Request/Response DTOs**: Controllers use dedicated request/response objects, never entities directly
- **Model objects**: Used as transfer objects between service and repository layers
- **ModelMapper**: Used for mapping between entities, models, and DTOs
- **Lombok**: All entities, models, and DTOs use `@Getter`, `@Setter`, `@Builder`, `@AllArgsConstructor`, etc.
- **Centralized constants**: Routes in `ApiRoutes`, defaults in `Consts`, messages in `MessageConst`

### Security Model

- Spring Security with form-based login
- BCrypt password encoding
- Account settings pages restricted to the owning user via SpEL expression
- Max 1 concurrent session per user
- Static resources (`/css/**`, `/js/**`, `/image/**`) are publicly accessible
- Photo browsing is publicly accessible; editing requires authentication

### User Authority Levels

| Level         | Description                          | Photo Upload Limit |
|---------------|--------------------------------------|--------------------|
| MINI          | Basic user                           | 10 photos          |
| NORMAL        | Standard user                        | 1,000 photos       |
| SPECIAL       | Premium user                         | Unlimited          |
| ADMINISTRATOR | Site administrator                   | Unlimited          |

### Error Code Convention

- `E-C-xxxx` - Common/account errors (e.g., `E-C-0001` = account registration failure)
- `E-P-xxxx` - Photo-related errors (e.g., `E-P-0001` = photo registration failure)
- All error codes defined in `enumuration/ErrorEnum.java` with messages in `MessageConst`

### Database Schema

Two PostgreSQL schemas:
- **`common`** schema: `account`, `kbn_mst` (classification master), `location_mst`
- **`photo`** schema: `photo_mst` (photo metadata + EXIF), `photo_tag_mst`, `photo_favorite`

Initialization scripts in `db/` directory, orchestrated by `db/init/init-db.sh`.

## Testing Conventions

### Test Types

- **Unit tests**: Use `@ExtendWith(MockitoExtension.class)` with mocked dependencies
- **Integration tests**: Suffixed with `IntegrationTest`, located in `integration/` subdirectories
  - Use `@SpringBootTest` with `@ActiveProfiles("test")`
  - Use `@Transactional` for automatic rollback
  - Use `@Sql("/sql/...")` annotations to load test fixture data

### Test Database

- Separate database: `web_gallary_test` (configured in `application-test.yml`)
- Test SQL fixtures organized by layer in `backend/src/test/resources/sql/`
- Docker PostgreSQL must be running for integration tests

### Running Tests

```bash
# Run all tests
./backend/gradlew -p backend test

# Run a specific test class
./backend/gradlew -p backend test --tests "com.web.gallary.service.impl.PhotoServiceImplTest"
```

## Development Setup

1. Start PostgreSQL via Docker:
   ```bash
   docker-compose up -d
   ```
2. The database initializes automatically using scripts in `db/`
3. Run the application:
   ```bash
   ./backend/gradlew -p backend bootRun
   ```
4. Access at `http://localhost:8080`

## Conventions to Follow

### Naming

- Package names: lowercase, underscore-separated (e.g., `type_handler`)
- Classes: PascalCase with descriptive suffixes (`Controller`, `RestController`, `Service`, `ServiceImpl`, `Repository`, `RepositoryImpl`, `Mapper`)
- Integration test classes: suffixed with `IntegrationTest`
- Constants: `UPPER_SNAKE_CASE`

### Code Style

- JavaDoc comments in Japanese for all public classes and methods
- Lombok annotations to reduce boilerplate (prefer `@Builder`, `@Getter`, `@Setter`)
- Entityクラスには `@Data` と `@Builder` のみを使用する（`@NoArgsConstructor` や `@AllArgsConstructor` は使用しない）
- Interface-based design for services and repositories
- No explicit linting or formatting tools configured; follow existing code style

### Adding New Features

1. Define routes in `ApiRoutes.java`
2. Create request/response DTOs in `controller/request/` and `controller/response/`
3. Create entity in `entity/` if new table is involved
4. Create model objects in `model/` for inter-layer transfer
5. Create MyBatis mapper interface in `mapper/` and XML in `resources/com/web/gallary/mapper/`
6. Create repository interface + implementation in `repository/` and `repository/impl/`
7. Create service interface + implementation in `service/` and `service/impl/`
8. Create controller in `controller/`
9. Add unit tests and integration tests following existing patterns
10. Add test SQL fixtures in `backend/src/test/resources/sql/`

### Important Notes

- The package name `enumuration` (not `enumeration`) is an intentional project convention - do not rename it
- File upload limit is 5MB per file (6MB at servlet level)
- Photo output path is configurable via `app.photo.outputPath` in `backend/src/main/resources/application.yml`
- The project uses WAR packaging for Tomcat deployment (not executable JAR)
- `backend/build.gradle` group is `com.official`, base package is `com.web.gallary`
