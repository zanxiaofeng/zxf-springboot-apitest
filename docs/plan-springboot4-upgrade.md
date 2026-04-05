# Spring Boot 4.0.5 升级 + Micrometer/OpenTelemetry/Logback Demo 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将项目从 Spring Boot 3.5.11 升级到 4.0.5，并添加 Micrometer、OpenTelemetry 和 Logback 的演示功能。

**Architecture:** 基于 Spring Boot 4.0 的新模块化 Starter 体系，使用 `spring-boot-starter-webmvc` + `spring-boot-starter-restclient` 替代旧的 `spring-boot-starter-web`。测试基础设施适配新的 `TestRestTemplate` 包路径和 `@AutoConfigureTestRestTemplate` 注解。新增可观测性演示（Micrometer 指标、OpenTelemetry 分布式追踪、Logback 结构化日志）。

**Tech Stack:** Spring Boot 4.0.5, Spring Framework 7.0, Jakarta EE 11, Jackson 3, Micrometer, OpenTelemetry, Logback

---

## 迁移要点总结

| 变更项 | 旧值 (3.5.11) | 新值 (4.0.5) |
|--------|---------------|--------------|
| Spring Boot 版本 | 3.5.11 | 4.0.5 |
| `spring-boot-starter-web` | 直接使用 | → `spring-boot-starter-webmvc` |
| RestTemplate 支持 | 包含在 web starter 中 | 需额外加 `spring-boot-starter-restclient` |
| `TestRestTemplate` 包 | `org.springframework.boot.test.web.client` | `org.springframework.boot.resttestclient` |
| `@AutoConfigureTestRestTemplate` | 不需要 | 必须添加 |
| Jackson | Jackson 2 (`com.fasterxml.jackson`) | Jackson 3 (`tools.jackson`) |
| Micrometer Starter | `micrometer-registry-*` | `spring-boot-starter-micrometer-metrics` |
| OpenTelemetry Starter | 手动集成 | `spring-boot-starter-opentelemetry` |

---

## Task 1: 升级 pom.xml 到 Spring Boot 4.0.5

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: 更新 parent 版本**

将 `pom.xml` 中 parent 版本从 `3.5.11` 改为 `4.0.5`：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.5</version>
    <relativePath/>
</parent>
```

- [ ] **Step 2: 更新 dependencies — 替换 starter 名称**

完整替换 `<dependencies>` 部分：

```xml
<dependencies>
    <!-- ==================== Main Dependencies ==================== -->

    <!-- Spring Boot Web MVC (替代 spring-boot-starter-web) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <!-- Spring Boot RestTemplate/RestClient 支持 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-restclient</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Spring Boot Data JDBC -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jdbc</artifactId>
    </dependency>

    <!-- Micrometer Metrics -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-micrometer-metrics</artifactId>
    </dependency>

    <!-- OpenTelemetry -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-opentelemetry</artifactId>
    </dependency>

    <!-- Actuator -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- MySQL Driver (Runtime) -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>dev.blaauwendraad</groupId>
        <artifactId>json-masker</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>

    <!-- Apache Commons Lang3 -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </dependency>

    <!-- ==================== Test Dependencies ==================== -->

    <!-- Spring Boot Web MVC Test (brings spring-boot-starter-test) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- TestRestTemplate (Spring Boot 4 新模块) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-resttestclient</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- H2 Database (Test Only) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- WireMock Spring Boot Starter -->
    <dependency>
        <groupId>org.wiremock.integrations</groupId>
        <artifactId>wiremock-spring-boot</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

注意：移除了旧的 `spring-boot-starter-test`，因为 `spring-boot-starter-webmvc-test` 已传递引入它。新增了 `spring-boot-starter-micrometer-metrics`、`spring-boot-starter-opentelemetry`、`spring-boot-starter-actuator`。

- [ ] **Step 3: 验证编译**

Run: `JAVA_HOME=/home/davis/.jdks/ms-21.0.10 mvn compile -q`
Expected: BUILD SUCCESS（可能有 import 警告但不影响编译）

---

## Task 2: 修复测试基础设施

**Files:**
- Modify: `src/test/java/zxf/springboot/demo/apitest/support/BaseApiTest.java`

- [ ] **Step 1: 更新 BaseApiTest 的 import 和注解**

1. 添加 `@AutoConfigureTestRestTemplate` 注解
2. 更新 `TestRestTemplate` 的 import 路径

完整文件内容：

```java
package zxf.springboot.demo.apitest.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplateAutoConfiguration;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import zxf.springboot.demo.apitest.support.sql.DatabaseVerifier;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Sql(scripts = {"classpath:sql/cleanup/clean-up.sql", "classpath:sql/init/schema.sql", "classpath:sql/init/data.sql"})
public abstract class BaseApiTest {
    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected DatabaseVerifier databaseVerifier;

    // ==================== GET Methods ====================

    protected <T> ResponseEntity<T> httpGetAndAssert(String url, HttpHeaders requestHeaders, Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        RequestEntity<String> requestEntity = new RequestEntity<>(null, requestHeaders, HttpMethod.GET, URI.create(url));
        ResponseEntity<T> responseEntity = testRestTemplate.exchange(requestEntity, tClass);
        assertThat(responseEntity.getStatusCode()).isEqualTo(expectedStatus);
        if (expectedContentType != null) {
            assertTrue(expectedContentType.isCompatibleWith(responseEntity.getHeaders().getContentType()));
        }
        return responseEntity;
    }

    // ==================== POST Methods ====================

    protected <T> ResponseEntity<T> httpPostAndAssert(String url, HttpHeaders requestHeaders, String requestBody, Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, requestHeaders, HttpMethod.POST, URI.create(url));
        ResponseEntity<T> responseEntity = testRestTemplate.exchange(requestEntity, tClass);
        assertThat(responseEntity.getStatusCode()).isEqualTo(expectedStatus);
        if (expectedContentType != null) {
            assertTrue(expectedContentType.isCompatibleWith(responseEntity.getHeaders().getContentType()));
        }
        return responseEntity;
    }

    // ==================== PUT Methods ====================

    protected <T> ResponseEntity<T> httpPutAndAssert(String url, HttpHeaders requestHeaders, String requestBody, Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, requestHeaders, HttpMethod.PUT, URI.create(url));
        ResponseEntity<T> responseEntity = testRestTemplate.exchange(requestEntity, tClass);
        assertThat(responseEntity.getStatusCode()).isEqualTo(expectedStatus);
        if (expectedContentType != null) {
            assertTrue(expectedContentType.isCompatibleWith(responseEntity.getHeaders().getContentType()));
        }
        return responseEntity;
    }

    // ==================== DELETE Methods ====================

    protected <T> ResponseEntity<T> httpDeleteAndAssert(String url, HttpHeaders requestHeaders, Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        RequestEntity<String> requestEntity = new RequestEntity<>(null, requestHeaders, HttpMethod.DELETE, URI.create(url));
        ResponseEntity<T> responseEntity = testRestTemplate.exchange(requestEntity, tClass);
        assertThat(responseEntity.getStatusCode()).isEqualTo(expectedStatus);
        if (expectedContentType != null && responseEntity.getHeaders().getContentType() != null) {
            assertTrue(expectedContentType.isCompatibleWith(responseEntity.getHeaders().getContentType()));
        }
        return responseEntity;
    }

    protected HttpHeaders commonHeaders() {
        return new HttpHeaders();
    }

    protected HttpHeaders commonHeadersAndJson() {
        HttpHeaders headers = commonHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
```

关键变更：
- import 从 `org.springframework.boot.test.web.client.TestRestTemplate` → `org.springframework.boot.resttestclient.TestRestTemplate`
- 添加 `@AutoConfigureTestRestTemplate` 注解
- 移除 `@AutoConfigureTestRestTemplate` 多余的 import（使用注解即可）

- [ ] **Step 2: 运行测试验证**

Run: `JAVA_HOME=/home/davis/.jdks/ms-21.0.10 mvn test -q`
Expected: 所有测试通过

如果编译失败，根据具体错误修复 import 或依赖问题。可能的额外修复：
- WireMock 的 `@AutoConfigureWireMock` 如果有变化需要调整
- `spring-boot-starter-webmvc-test` 可能不包含 WireMock 所需的自动配置

---

## Task 3: 修复 application.yml 配置

**Files:**
- Modify: `src/main/resources/application.yml`
- Modify: `src/test/resources/application.yml`

- [ ] **Step 1: 更新主配置文件**

在 `src/main/resources/application.yml` 中：
1. 移除无用的 `jpa` 配置（项目不使用 JPA）
2. 添加 Actuator 配置
3. 添加 Micrometer 配置
4. 添加 OpenTelemetry 配置

```yaml
# Main application - MySQL configuration
server:
  port: 8080

# MySQL Database Configuration (Production)
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME:demo}
    password: ${DB_PASSWORD:}

  # SQL Init
  sql:
    init:
      mode: always
      schema-locations: classpath:sql/init/schema.sql
      data-locations: classpath:sql/init/data.sql
      continue-on-error: false

# Task Service Configuration
task-service:
  url: http://localhost:8090

# Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    tags:
      application: ${spring.application.name:zxf-springboot-apitest}
    export:
      prometheus:
        enabled: true
      simple:
        enabled: true
  tracing:
    enabled: true
    sampling:
      probability: 1.0

# OpenTelemetry
otel:
  exporter:
    otlp:
      endpoint: http://localhost:4317
      protocol: grpc
  traces:
    exporter: otlp

# Logging
logging:
  level:
    root: INFO
    zxf.springboot.demo: DEBUG
```

- [ ] **Step 2: 更新测试配置文件**

在 `src/test/resources/application.yml` 中：
1. 移除无用的 `h2.console` 配置
2. 添加测试用的 actuator/metrics/otel 配置
3. 添加 OpenTelemetry 配置（测试环境禁用 OTLP export）

```yaml
server:
  port: 8080

# H2 Database Configuration
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE;MODE=MYSQL;DATABASE_TO_LOWER=TRUE;
    driver-class-name: org.h2.Driver
    username: sa
    password: password

  # SQL Init Mode - disabled, using @Sql instead
  sql:
    init:
      mode: never

# Task Service Configuration
task-service:
  url: http://localhost:8090

# Actuator - test config
management:
  endpoints:
    web:
      exposure:
        include: health,metrics
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: false
      simple:
        enabled: true
  tracing:
    enabled: false

# OpenTelemetry - disabled for tests
otel:
  traces:
    exporter: none

zxf:
  trace:
    inbound:
      enabled: true
      logging: true
      mdc-injection:
        injections:
          - key: ENV
            header: X-ENV
          - key: REQ_ID
            header: X-Request-Id
    outbound:
      enabled: true
    sensitive-mask:
      headers:
        - Token
        - Authorization
        - Cookie
        - Set-Cookie
        - host
        - Matched-Stub-Id
        - user-agent
      json-names:
        - email
        - token
        - password

# Logging Configuration
logging:
  level:
    root: ERROR
    zxf.springboot.demo: DEBUG
    org.springframework.test.context: DEBUG
    org.springframework.jdbc.core: DEBUG
```

- [ ] **Step 3: 验证测试仍然通过**

Run: `JAVA_HOME=/home/davis/.jdks/ms-21.0.10 mvn test -q`
Expected: 所有测试通过

---

## Task 4: 添加 Micrometer 演示

**Files:**
- Create: `src/main/java/zxf/springboot/demo/observability/MetricsDemoService.java`
- Create: `src/main/java/zxf/springboot/demo/controller/MetricsDemoController.java`

- [ ] **Step 1: 创建 MetricsDemoService**

`src/main/java/zxf/springboot/demo/observability/MetricsDemoService.java`:

```java
package zxf.springboot.demo.observability;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class MetricsDemoService {
    private final Counter demoCounter;
    private final AtomicInteger activeUsers;

    public MetricsDemoService(MeterRegistry meterRegistry) {
        this.demoCounter = meterRegistry.counter("demo.counter", "type", "example");
        this.activeUsers = meterRegistry.gauge("demo.active.users", new AtomicInteger(0));
    }

    @Timed(value = "demo.operation.time", description = "Time spent on demo operation", percentiles = {0.5, 0.95, 0.99})
    public String timedOperation() {
        log.info("Executing timed operation");
        try {
            Thread.sleep((long) (Math.random() * 200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Timed operation completed";
    }

    @Counted(value = "demo.operation.count", description = "Number of times counted operation was invoked")
    public String countedOperation() {
        log.info("Executing counted operation");
        demoCounter.increment();
        return "Counted operation completed (count: " + demoCounter.count() + ")";
    }

    public int updateActiveUsers(int delta) {
        int newValue = activeUsers.updateAndGet(v -> Math.max(0, v + delta));
        log.info("Active users updated: {}", newValue);
        return newValue;
    }
}
```

- [ ] **Step 2: 创建 MetricsDemoController**

`src/main/java/zxf/springboot/demo/controller/MetricsDemoController.java`:

```java
package zxf.springboot.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zxf.springboot.demo.observability.MetricsDemoService;

import java.util.Map;

@RestController
@RequestMapping("/api/demo/metrics")
@RequiredArgsConstructor
public class MetricsDemoController {
    private final MetricsDemoService metricsDemoService;

    @GetMapping("/timed")
    public ResponseEntity<Map<String, String>> timedOperation() {
        String result = metricsDemoService.timedOperation();
        return ResponseEntity.ok(Map.of("result", result));
    }

    @GetMapping("/counted")
    public ResponseEntity<Map<String, String>> countedOperation() {
        String result = metricsDemoService.countedOperation();
        return ResponseEntity.ok(Map.of("result", result));
    }

    @PostMapping("/active-users")
    public ResponseEntity<Map<String, Object>> updateActiveUsers(@RequestParam int delta) {
        int active = metricsDemoService.updateActiveUsers(delta);
        return ResponseEntity.ok(Map.of("activeUsers", active));
    }
}
```

- [ ] **Step 3: 验证编译和测试**

Run: `JAVA_HOME=/home/davis/.jdks/ms-21.0.10 mvn compile -q`
Expected: BUILD SUCCESS

Run: `JAVA_HOME=/home/davis/.jdks/ms-21.0.10 mvn test -q`
Expected: 所有原有测试通过

---

## Task 5: 添加 OpenTelemetry 演示

**Files:**
- Create: `src/main/java/zxf/springboot/demo/observability/TracingDemoService.java`
- Create: `src/main/java/zxf/springboot/demo/controller/TracingDemoController.java`

- [ ] **Step 1: 创建 TracingDemoService**

`src/main/java/zxf/springboot/demo/observability/TracingDemoService.java`:

```java
package zxf.springboot.demo.observability;

import io.micrometer.tracing.SpanName;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TracingDemoService {
    private final Tracer tracer;

    @SpanName("demo.tracing.parent")
    public String tracedOperation(String input) {
        var currentSpan = tracer.currentSpan();
        String traceId = currentSpan != null ? currentSpan.context().traceId() : "no-trace";
        String spanId = currentSpan != null ? currentSpan.context().spanId() : "no-span";

        log.info("Traced operation - traceId: {}, spanId: {}, input: {}", traceId, spanId, input);

        childOperation(input);

        return "Traced: traceId=%s, spanId=%s, input=%s".formatted(traceId, spanId, input);
    }

    private void childOperation(String input) {
        var newSpan = tracer.nextSpan().name("demo.tracing.child").start();
        try (var ignored = tracer.withSpan(newSpan)) {
            log.info("Child span processing: {}", input.toUpperCase());
        } finally {
            newSpan.end();
        }
    }
}
```

- [ ] **Step 2: 创建 TracingDemoController**

`src/main/java/zxf/springboot/demo/controller/TracingDemoController.java`:

```java
package zxf.springboot.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zxf.springboot.demo.observability.TracingDemoService;

import java.util.Map;

@RestController
@RequestMapping("/api/demo/tracing")
@RequiredArgsConstructor
public class TracingDemoController {
    private final TracingDemoService tracingDemoService;

    @GetMapping("/{input}")
    public ResponseEntity<Map<String, String>> tracedOperation(@PathVariable String input) {
        String result = tracingDemoService.tracedOperation(input);
        return ResponseEntity.ok(Map.of("result", result));
    }
}
```

- [ ] **Step 3: 验证编译和测试**

Run: `JAVA_HOME=/home/davis/.jdks/ms-21.0.10 mvn compile -q`
Expected: BUILD SUCCESS

---

## Task 6: 添加 Logback 结构化日志演示

**Files:**
- Create: `src/main/resources/logback-spring.xml`

- [ ] **Step 1: 创建 logback-spring.xml**

`src/main/resources/logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <!-- ==================== Console Appender (Pattern Layout) ==================== -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleLayout">
        <pattern>
            ${CONSOLE_LOG_PATTERN:-%clr(%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} [%X{traceId:-},%X{spanId:-}] %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}}
        </pattern>
        <charset>UTF-8</charset>
    </appender>

    <!-- ==================== JSON File Appender (Structured Logging) ==================== -->
    <appender name="JSON_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH:-./logs}/application.json</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH:-./logs}/application.%d{yyyy-MM-dd}.%i.json</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>7</maxHistory>
            <totalSizeCap>100MB</totalSizeCap>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeContext>true</includeContext>
            <includeMdc>true</includeMdc>
            <includeStructuredArguments>true</includeStructuredArguments>
            <includeNonStructuredArguments>false</includeNonStructuredArguments>
            <includeTags>true</includeTags>
            <includeCallerData>false</includeCallerData>
            <customFields>{"app_name":"zxf-springboot-apitest","env":"${SPRING_PROFILES_ACTIVE:-default}"}</customFields>
        </encoder>
    </appender>

    <!-- ==================== Profile-based Configuration ==================== -->

    <!-- Default profile: console only with trace IDs -->
    <springProfile name="default">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
        <logger name="zxf.springboot.demo" level="DEBUG"/>
    </springProfile>

    <!-- Dev profile: console + JSON file -->
    <springProfile name="dev">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="JSON_FILE"/>
        </root>
        <logger name="zxf.springboot.demo" level="DEBUG"/>
    </springProfile>

    <!-- Test profile: minimal logging -->
    <springProfile name="test">
        <root level="ERROR">
            <appender-ref ref="CONSOLE"/>
        </root>
        <logger name="zxf.springboot.demo" level="DEBUG"/>
    </springProfile>
</configuration>
```

注意：JSON encoder 使用 `logstash-logback-encoder`，需要在 pom.xml 中添加依赖（见 Step 2）。

- [ ] **Step 2: 在 pom.xml 添加 logstash-logback-encoder**

在 `dependencyManagement` 中添加版本管理，在 `dependencies` 中添加依赖：

```xml
<!-- 在 dependencyManagement > dependencies 中添加 -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>8.0</version>
</dependency>

<!-- 在 dependencies 中添加 -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <scope>runtime</scope>
</dependency>
```

- [ ] **Step 3: 创建 Logback 演示 Controller**

`src/main/java/zxf/springboot/demo/controller/LogbackDemoController.java`:

```java
package zxf.springboot.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/demo/logging")
public class LogbackDemoController {

    @GetMapping("/demo")
    public ResponseEntity<Map<String, String>> loggingDemo(@RequestParam(defaultValue = "world") String name) {
        // 结构化日志演示：使用 MDC 添加上下文信息
        MDC.put("demo.user", name);

        log.info("Processing logging demo request");
        log.debug("Debug level message with parameter: name={}", name);
        log.warn("Warning: this is a structured log demo");

        String result = "Hello, %s! Check logs for structured output.".formatted(name);
        MDC.remove("demo.user");

        return ResponseEntity.ok(Map.of("result", result));
    }

    @GetMapping("/levels")
    public ResponseEntity<Map<String, String>> logLevels() {
        log.trace("TRACE level message");
        log.debug("DEBUG level message");
        log.info("INFO level message");
        log.warn("WARN level message");
        log.error("ERROR level message");
        return ResponseEntity.ok(Map.of("result", "All log levels demonstrated. Check console output."));
    }
}
```

- [ ] **Step 4: 验证编译和测试**

Run: `JAVA_HOME=/home/davis/.jdks/ms-21.0.10 mvn compile -q`
Expected: BUILD SUCCESS

---

## Task 7: 运行全量测试 + 最终验证

**Files:**
- None (verification only)

- [ ] **Step 1: 运行完整编译**

Run: `JAVA_HOME=/home/davis/.jdks/ms-21.0.10 mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 2: 运行全量测试**

Run: `JAVA_HOME=/home/davis/.jdks/ms-21.0.10 mvn test`
Expected: 所有测试通过，无编译错误

- [ ] **Step 3: 修复任何剩余问题**

如果测试失败，根据错误信息修复：
- WireMock 兼容性问题：检查 wiremock-spring-boot 4.0.9 是否支持 Spring Boot 4.0.5
- import 问题：检查是否有遗漏的包路径变更
- 配置属性变更：检查 application.yml 中是否有已废弃的属性

- [ ] **Step 4: 提交**

```bash
git add -A
git commit -m "feat: 升级 Spring Boot 4.0.5 + 添加 Micrometer/OpenTelemetry/Logback 演示

- 升级 Spring Boot 3.5.11 → 4.0.5
- 替换 spring-boot-starter-web → spring-boot-starter-webmvc + spring-boot-starter-restclient
- 适配 TestRestTemplate 新包路径 + @AutoConfigureTestRestTemplate
- 添加 spring-boot-starter-micrometer-metrics + 指标演示 API
- 添加 spring-boot-starter-opentelemetry + 分布式追踪演示 API
- 添加 logback-spring.xml 结构化日志 + logstash-logback-encoder
- 添加 /api/demo/metrics, /api/demo/tracing, /api/demo/logging 演示端点"
```

---

## 文件变更总览

| 操作 | 文件路径 |
|------|----------|
| Modify | `pom.xml` |
| Modify | `src/main/resources/application.yml` |
| Modify | `src/test/resources/application.yml` |
| Modify | `src/test/java/.../support/BaseApiTest.java` |
| Create | `src/main/java/.../observability/MetricsDemoService.java` |
| Create | `src/main/java/.../observability/TracingDemoService.java` |
| Create | `src/main/java/.../controller/MetricsDemoController.java` |
| Create | `src/main/java/.../controller/TracingDemoController.java` |
| Create | `src/main/java/.../controller/LogbackDemoController.java` |
| Create | `src/main/resources/logback-spring.xml` |
