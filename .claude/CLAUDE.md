# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
./gradlew build

# Run application
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.luongnm93.my_spring_batch.MySpringBatchApplicationTests"

# Run a single test method
./gradlew test --tests "com.luongnm93.my_spring_batch.MySpringBatchApplicationTests.methodName"

# Clean build
./gradlew clean build
```

## Tech Stack

- **Java 25**, Spring Boot 4.0.6
- **Spring Batch** (`spring-boot-starter-batch`, `spring-boot-starter-batch-jdbc`) — core batch processing framework
- **Spring Web MVC** (`spring-boot-starter-webmvc`) — HTTP endpoints to trigger/monitor jobs
- **JUnit 5** via `junit-platform-launcher`
- **Lombok** — used project-wide to eliminate boilerplate (getters, setters, constructors, builders, loggers)

## Architecture

This is a Spring Batch application. The standard pattern for this project is:

- **Jobs** — top-level batch units, composed of one or more Steps, defined as `@Bean Job` in `@Configuration` classes
- **Steps** — each Step is either chunk-oriented (`ItemReader` → `ItemProcessor` → `ItemWriter`) or tasklet-based
- **Web layer** — MVC controllers used to trigger job execution via `JobLauncher` and expose job status
- **Batch metadata** — Spring Batch requires a datasource for its schema (job execution tables); configure `spring.datasource.*` in `application.properties` and run the batch schema scripts, or use an in-memory H2 for dev

Base package: `com.luongnm93.my_spring_batch`

## Coding Conventions

### Package Structure (Domain-Driven Design)

- Organize packages by **domain/feature**, not by layer. Each domain owns all its layers internally.
- Within each domain package, use dedicated sub-packages for each layer:
  - `entity/` — JPA `@Entity` classes
  - `repository/` — Spring Data `Repository` interfaces
  - `service/` — `@Service` classes containing business logic
  - `dto/` — DTO classes for data crossing layer boundaries
  - `controller/` — `@RestController` / `@Controller` classes (when applicable)
  - `job/` — Spring Batch `Job`, `Step`, `ItemReader`, `ItemProcessor`, `ItemWriter`, and `Tasklet` classes specific to this domain
- Example structure:
  ```
  com.luongnm93.my_spring_batch
  ├── config/              # All @Configuration classes (batch, datasource, web, etc.)
  ├── security/            # Security configuration, filters, and related classes
  ├── employee/            # Employee domain
  │   ├── entity/
  │   │   └── Employee.java
  │   ├── repository/
  │   │   └── EmployeeRepository.java
  │   ├── service/
  │   │   ├── EmployeeService.java
  │   │   └── EmployeeServiceImpl.java
  │   ├── dto/
  │   │   └── EmployeeDto.java
  │   └── job/
  │       ├── EmployeeImportJobConfig.java
  │       ├── EmployeeItemReader.java
  │       ├── EmployeeItemProcessor.java
  │       └── EmployeeItemWriter.java
  └── order/               # Order domain
      ├── entity/
      ├── repository/
      ├── service/
      ├── dto/
      └── job/
  ```
- **`config` package** — all Spring `@Configuration` classes that are cross-cutting or infrastructure-level (e.g., `BatchConfig`, `DataSourceConfig`, `WebMvcConfig`).
- **`security` package** — all security-related classes: `SecurityConfig`, custom filters, authentication/authorization handlers, and security utilities.
- Never place configuration or security classes inside a domain package; they belong in `config` or `security` at the base package level.
- Never place an `@Entity`, `Repository`, or `@Service` directly in the domain root — always in the appropriate sub-package.
- All batch jobs, steps, readers, processors, and writers that belong to a domain must live in that domain's `job/` sub-package — never in the top-level `config/` package.

### Service Layer Convention

- Every service must be defined as an **interface** first, then implemented by a class named `<InterfaceName>Impl`.
- Example: `GzFileStorageService` (interface) → `GzFileStorageServiceImpl` (implementation).
- Both files live in the same `service/` sub-package of their domain.
- Inject services by the **interface type**, never the implementation class.
- Place `@Service` only on the `Impl` class, not on the interface.

### Data Transfer (DTO pattern)

- Use dedicated DTO classes for all data crossing layer boundaries — never expose domain/entity objects directly to the web layer or batch readers/writers.
- Naming: suffix with `Dto` (e.g., `UserDto`, `OrderItemDto`). Use separate classes for input vs. output when their shapes differ (e.g., `CreateUserDto` vs. `UserResponseDto`).
- Place DTOs in a `dto` sub-package alongside the feature they belong to (e.g., `com.luongnm93.my_spring_batch.user.dto`).
- Keep DTOs plain data holders — no business logic. Mapping between DTOs and domain objects belongs in a dedicated mapper class or static factory method on the DTO itself.
- In chunk-oriented Steps, the `ItemProcessor` is the canonical place to convert an input DTO (read from a file/DB) into a domain object or output DTO before writing.

### Lombok Conventions

- All `@Entity` and DTO classes must use `@Getter` / `@Setter` instead of hand-written accessors.
- Use `@Builder` on DTOs and value objects; **avoid** it on JPA entities — Hibernate proxies require a no-arg constructor and `@Builder` conflicts with that.
- Use `@RequiredArgsConstructor` on `@Service`, `@Configuration`, and `@RestController` classes — Spring will use the single constructor for injection; do not add `@Autowired`.
- Use `@Slf4j` for logging; never declare a `Logger` field manually.
- Do **not** use `@Data` on JPA entities — its generated `equals`/`hashCode` iterates all fields and breaks with lazy-loaded proxies. Use `@Getter @Setter @ToString(exclude = "collectionField")` explicitly instead.
- `@Value` (Lombok) is acceptable on immutable DTOs or configuration records.

### Dependency Version Convention

- This project uses Spring Boot **4.0.6** with the `io.spring.dependency-management` plugin. All Spring-ecosystem and common third-party libraries managed by the Spring BOM must **not** include an explicit version in `build.gradle` — rely on BOM-managed versions.
- Before adding a new dependency, verify it is BOM-managed: run `./gradlew dependencyManagement` or check the Spring Boot 4.0.6 release notes.
- If a library is **not** in the BOM, pin an explicit version and add an inline comment explaining why it is unmanaged.
- Never downgrade a BOM-managed dependency without a documented reason.
