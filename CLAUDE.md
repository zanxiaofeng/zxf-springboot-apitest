# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 3.5 demo project demonstrating API testing best practices with WireMock and H2 in-memory database.

## Commands

```bash
# Build
JAVA_HOME=/home/davis/.jdks/ms-21.0.10 mvn compile

# Run all tests
JAVA_HOME=/home/davis/.jdks/ms-21.0.10 mvn test

# Run single test class
JAVA_HOME=/home/davis/.jdks/ms-21.0.10 mvn test -Dtest=ProjectApiTests

# Run single test method
JAVA_HOME=/home/davis/.jdks/ms-21.0.10 mvn test -Dtest=ProjectApiTests#testCreateProject

# Run application (requires MySQL - use docker-compose)
docker-compose up -d
JAVA_HOME=/home/davis/.jdks/ms-21.0.10 mvn spring-boot:run
```

## Architecture

```
Controller → Service → DatabaseService/TaskService → JdbcTemplate/TaskServiceClient
                ↓
        TaskServiceClient → RestTemplate → External task-service (mocked by WireMock)
```

### Key Components

- **Controllers**: `ProjectController` (`/api/projects`), `TaskController` (`/api/tasks`)
- **Services**: `DatabaseService` (JDBC operations), `TaskService` (orchestrates DB + external calls)
- **TaskServiceClient**: Calls downstream `task-service` at port 8090 (mocked in tests via WireMock)

### Test Infrastructure

| Component | Purpose |
|-----------|---------|
| `BaseApiTest` | Abstract base with HTTP helper methods (`httpGetAndAssert`, `httpPostAndAssert`, etc.) |
| `JsonLoader` | Loads JSON from `test-data/` with `${variable}` template support |
| `JSONComparatorFactory` | Creates comparators that ignore dynamic fields (id, timestamp, downstreamResponse) |
| `TaskServiceMockFactory` | WireMock stubs for external `task-service` |

### Test Data Organization

```
src/test/resources/
├── application.yml          # H2 config, WireMock port 8090
├── sql/                     # Database init scripts
│   ├── cleanup/clean-up.sql
│   └── init/schema.sql, data.sql
├── test-data/               # JSON fixtures organized by endpoint
│   ├── project/
│   │   ├── post/            # request.json, created.json, conflict.json, validation-error.json
│   │   ├── get-by-id/       # ok.json, not-found.json
│   │   └── ...
│   └── task/...
└── mock-data/               # WireMock static mappings (for file-based mocks)
```

## Testing Patterns

```java
// Load request template with variables
String requestBody = JsonLoader.load("project/post/request.json",
        Map.of("id", "proj-new", "name", "New Project"));

// Execute HTTP call with assertion
ResponseEntity<String> response = httpPostAndAssert(
        url, commonHeadersAndJson(), requestBody,
        String.class, HttpStatus.CREATED, MediaType.APPLICATION_JSON);

// Assert response with JSON comparator (ignores dynamic fields)
String expectedJson = JsonLoader.load("project/post/created.json");
JSONAssert.assertEquals(expectedJson, response.getBody(), comparator);

// Mock external service
TaskServiceMockFactory.mockCreateTaskSuccess(taskName, responseBody);
```

## Trace Infrastructure

`InboundLoggingFilter` and `OutboundLoggingInterceptor` provide request/response logging with:
- MDC injection (X-ENV, X-Request-Id headers)
- Sensitive data masking (headers: Token, Authorization; JSON fields: email, password, token)

Configured via `zxf.trace.inbound.*` and `zxf.trace.outbound.*` properties.