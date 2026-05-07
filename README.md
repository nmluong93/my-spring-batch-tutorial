# my-spring-batch

A Spring Batch application that imports large CSV files into a MySQL database using partitioned, parallel processing with Java virtual threads.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.6 |
| Batch | Spring Batch (chunk-oriented, partitioned) |
| Web | Spring Web MVC |
| Database | MySQL 8+ |
| Test DB | H2 (in-memory, test scope only) |
| Boilerplate | Lombok |
| Containerization | Docker + Docker Compose |

---

## Prerequisites

**Docker (recommended)**
- Docker 24+ and Docker Compose v2+

**Local / bare-metal**
- Java 25+
- MySQL 8+ running on `localhost:3306`
- Gradle (wrapper included — no separate install needed)

---

## Spring Profiles

The application uses Spring profiles to separate environment-specific configuration:

| Profile | Config file | Upload directory | When to use |
|---|---|---|---|
| `dev` | `application-dev.yml` | `~/my-spring-batch/uploads` | Local development |
| `docker` | `application-docker.yml` | `/var/job/source` | Docker / production |

---

## Running with Docker Compose (recommended)

All services (MySQL + the Spring Boot app) are defined in `docker/docker-compose.yaml`. The `docker` profile is activated automatically — no extra steps needed.

```bash
docker compose -f docker/docker-compose.yaml up --build
```

The app will be available at `http://localhost:8080` once the MySQL healthcheck passes and the app container starts.

**Stop and remove containers (data is preserved in the `mysql_data` volume):**

```bash
docker compose -f docker/docker-compose.yaml down
```

**Destroy all data too:**

```bash
docker compose -f docker/docker-compose.yaml down -v
```

### Environment variables

Override defaults by setting environment variables before running Compose:

| Variable | Default | Description |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | `root` | MySQL root password |
| `MYSQL_DATABASE` | `my-spring-batch` | Database name |

```bash
MYSQL_ROOT_PASSWORD=secret MYSQL_DATABASE=batchdb \
  docker compose -f docker/docker-compose.yaml up --build
```

### Docker details

| File | Purpose |
|---|---|
| `docker/Dockerfile` | Multi-stage build: `eclipse-temurin:25-jdk` compiles the fat JAR, `eclipse-temurin:25-jre` runs it |
| `docker/docker-compose.yaml` | Orchestrates `mysql:8.4` + the app; health-checks MySQL before starting the app; sets `SPRING_PROFILES_ACTIVE=docker` |

The app container exposes port `8080`. MySQL is accessible on host port `3306` for local tooling.

A named volume `job_source` is mounted at `/var/job/source` inside the app container — place CSV files there to import them without rebuilding the image.

---

## Running Locally (without Docker)

### Database Setup

The application auto-creates the database and schema on startup via `spring.sql.init` and Spring Batch's own metadata tables. You only need a running MySQL instance with the credentials below (or adjust `application.yml`):

| Setting | Default |
|---|---|
| Host | `localhost:3306` |
| Database | `my-spring-batch` |
| Username | `root` |
| Password | `root` |

To use different credentials, edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/my-spring-batch?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: your-username
    password: your-password
```

### Run

The `dev` profile is active by default — no extra flags needed. Upload files land in `~/my-spring-batch/uploads` (created automatically on first run).

```bash
# Build
./gradlew build

# Run the application (starts on http://localhost:8080)
./gradlew bootRun

# Clean build
./gradlew clean build
```

The application does **not** auto-run any batch job on startup (`spring.batch.job.enabled=false`). Jobs are triggered via REST API.

---

## REST API

The typical workflow is **upload first, then import**:

1. `POST /api/employees/upload` — upload a `.gz`-compressed CSV to the server
2. `POST /api/employees/import` — trigger the batch job against the uploaded file

---

### 1. Upload CSV File

Uploads a `.gz`-compressed CSV file to the server's upload directory (configured via `batch.upload.directory` — `~/my-spring-batch/uploads` for `dev`, `/var/job/source` for `docker`).

**Endpoint**

```
POST /api/employees/upload
Content-Type: multipart/form-data
```

**Form field**

| Field | Type | Required | Description |
|---|---|---|---|
| `file` | file | Yes | A `.gz`-compressed CSV file. Only `.gz` files are accepted. |

**Success response — 200 OK**

```json
{
  "fileName": "employees_1000000_rows.csv.gz"
}
```

Use the returned `fileName` value as the `fileName` field in the import request below.

**Error response — 400 Bad Request** (non-`.gz` file or unsafe filename)

```json
{
  "error": "Only .gz files are accepted"
}
```

**Error response — 500 Internal Server Error**

```json
{
  "error": "<exception message>"
}
```

**Example curl**

```bash
curl -X POST http://localhost:8080/api/employees/upload \
  -F "file=@employees_1000000_rows.csv.gz"
```

---

### 2. Trigger Employee Import Job

Launches the `employeeImportJob` to decompress and import the uploaded file into the `employees` table.

**Endpoint**

```
POST /api/employees/import
Content-Type: application/json
```

**Request body**

```json
{
  "fileName": "employees_1000000_rows.csv.gz"
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `fileName` | `string` | Yes | Filename returned by the upload endpoint (must already exist in the upload directory) |

**Success response — 202 Accepted**

```json
{
  "jobExecutionId": 1,
  "status": "STARTED"
}
```

**Conflict response — 409 Conflict**

Returned when the same `fileName` has already been imported successfully (Spring Batch prevents duplicate job instances with identical parameters).

```json
{
  "error": "Job already completed successfully for this file. No action taken."
}
```

**Error response — 500 Internal Server Error**

```json
{
  "error": "<exception message>"
}
```

**Example curl**

```bash
curl -X POST http://localhost:8080/api/employees/import \
  -H "Content-Type: application/json" \
  -d '{"fileName": "employees_1000000_rows.csv.gz"}'
```

---

## Sample Data

Need a CSV to test with? Use the built-in **`generate-employee-csv`** Claude Code skill — just ask Claude to generate employee data and it will produce a ready-to-use CSV file of any size.

Once you have the CSV, compress it before uploading:

```bash
gzip employees.csv        # produces employees.csv.gz
```

---

## How the Batch Job Works

The `employeeImportJob` uses a **partitioned, parallel** strategy to process large files efficiently:

```
employeeImportJob
  └── employeeImportMasterStep   (partitions the file)
        ├── partition0  → employeeImportWorkerStep  (reads lines 1–100000)
        ├── partition1  → employeeImportWorkerStep  (reads lines 100001–200000)
        ├── ...
        └── partition9  → employeeImportWorkerStep  (reads lines 900001–1000000)
```

| Component | Description |
|---|---|
| `EmployeeCsvPartitioner` | Divides the CSV into N equal line ranges (default: 10 partitions) |
| `FlatFileItemReader` | Reads its assigned line range from the CSV, skipping the header |
| `EmployeeItemProcessor` | Maps `EmployeeCsvDto` → `Employee` entity |
| `JdbcBatchItemWriter` | Batch-inserts records into MySQL in chunks of 1000 |
| `VirtualThreadTaskExecutor` | Runs each partition on a Java virtual thread |

**Partition count** is controlled by `application.yml`:

```yaml
batch:
  partition:
    grid-size: 10   # increase/decrease to match your DB connection pool
```

**Chunk size** is hardcoded to `1000` rows per transaction in `EmployeeImportJobConfig`.

---

## Running Tests

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.luongnm93.my_spring_batch.employee.job.EmployeeImportJobTest"

# Run a specific test method
./gradlew test --tests "com.luongnm93.my_spring_batch.employee.job.EmployeeImportJobTest.methodName"
```

Tests use an **H2 in-memory database** (`src/test/resources/application-test.yml`) and a small fixture CSV (`src/test/resources/test-employees.csv`), so no MySQL instance is required to run them.

---

## Project Structure

```
src/main/java/com/luongnm93/my_spring_batch/
├── MySpringBatchApplication.java
├── employee/
│   ├── controller/
│   │   └── EmployeeImportController.java   # POST /api/employees/import
│   ├── dto/
│   │   ├── EmployeeCsvDto.java             # CSV row → Java object
│   │   └── EmployeeImportRequestDto.java   # REST request body
│   ├── entity/
│   │   └── Employee.java                   # JPA entity / DB row
│   ├── repository/
│   │   └── EmployeeRepository.java
│   └── job/
│       ├── EmployeeImportJobConfig.java    # Job, Step, Reader, Writer beans
│       ├── EmployeeCsvPartitioner.java     # File partitioning logic
│       └── EmployeeItemProcessor.java      # DTO → Entity mapping

src/main/resources/
├── application.yml                         # Shared config (all profiles)
├── application-dev.yml                     # Dev profile: local upload directory
├── application-docker.yml                  # Docker profile: /var/job/source
├── employees_1000000_rows.csv              # Bundled 1M-row dataset
└── sql/
    └── init-my-batch-job.sql               # employees table DDL
```
