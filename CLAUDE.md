# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 4.0.5 demo project demonstrating API testing best practices with WireMock, H2 in-memory database, and observability (Micrometer, OpenTelemetry, Logback).

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

- **Controllers**: `ProjectController` (`/api/projects`), `TaskController` (`/api/tasks`), `MetricsDemoController` (`/api/demo/metrics`), `TracingDemoController` (`/api/demo/tracing`), `LogbackDemoController` (`/api/demo/logging`)
- **Services**: `DatabaseService` (JDBC operations), `TaskService` (orchestrates DB + external calls) `MetricsDemoService` (Micrometer demos) `TracingDemoService` (OpenTelemetry tracing)
- **TaskServiceClient**: Calls downstream `task-service` at port 8090 (mocked in tests via WireMock)



### Test Infrastructure

| Component | Purpose |
|-----------|---------|
| `BaseApiTest` | Abstract base with HTTP helper methods (`httpGetAndAssert`, `httpPostAndAssert`, etc.) |
| `JsonLoader` | Loads JSON from `test-data/` with `${variable}` template support |
| `JSONComparatorFactory` | Creates comparators that ignore dynamic fields (id, timestamp, externalTaskId) |
| `TaskServiceMockFactory` | WireMock stubs for external `task-service` |
| `TaskServiceMockVerifier` | Verifies downstream service was called with expected parameters |

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

## Coding Principles

1. **可读性是第一原则** - Code is read more than written
2. **能用1行完成的代码绝不用2行** - Concise but not cryptic
3. **优先使用框架/库能力** - Don't reinvent the wheel:
   - a) JDK 特性优先 (如 text blocks, records, var)
   - b) Lombok 简化代码 (@Data, @Builder, @Slf4j)
   - c) Spring/Spring Boot 提供的类
   - d) Commons 库 (commons-lang3, commons-io)

## Spring Framework Patterns

### 优先使用框架内置能力

当遇到"需要处理编码/转义/格式化"时，先问：**框架是否已内置这个能力？**

| 场景 | 不要手动 | 使用框架 |
|------|----------|----------|
| URL 参数编码 | `URLEncoder.encode()` | `RestTemplate.getForEntity(url + "?name={name}", ..., taskName)` |
| JSON 序列化 | 手动拼接字符串 | `ObjectMapper` / `@RequestBody` |
| SQL 参数 | 字符串连接 | `JdbcTemplate` 参数占位符 `?` |
| HTML 转义 | 手动替换 | `HtmlUtils.htmlEscape()` 或模板引擎自动转义 |

**思维模式:** 遇到编码/转义问题时，先查框架 API，再考虑手动处理。