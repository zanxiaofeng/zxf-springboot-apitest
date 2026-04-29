# Spring Boot 3 Maven POM 最佳实践

**版本：** 1.0
**适用范围：** 所有基于 Spring Boot 3.x + Java 21+ 的 Maven 项目

---

## 1. 依赖管理优化

### 1.1 核心原则

| 原则 | 说明 |
|------|------|
| 继承 BOM | 通过 `spring-boot-starter-parent` 统一管理 Spring 生态版本 |
| 版本集中声明 | 所有非 BOM 管理的版本集中在 `<properties>` 中 |
| 显式 scope | 每个依赖声明明确的 `scope`，避免隐式 `compile` |
| 注释分组 | 按 `// === 分组 ===` 风格组织依赖，增强可读性 |

### 1.2 完整 POM 模板

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.11</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>my-service</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <!-- === 基础配置 === -->
        <java.version>21</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <!-- === 第三方依赖版本（BOM 未管理的） === -->
        <wiremock-spring-boot.version>4.0.9</wiremock-spring-boot.version>
        <json-masker.version>1.1.2</json-masker.version>
        <commons-lang3.version>3.17.0</commons-lang3.version>

        <!-- === 插件版本 === -->
        <jacoco.version>0.8.12</jacoco.version>

        <!-- === API 版本（详见第 3 节） === -->
        <api.major>1</api.major>
        <api.minor>0</api.minor>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- BOM 未覆盖的第三方依赖在此统一版本 -->
            <dependency>
                <groupId>org.wiremock.integrations</groupId>
                <artifactId>wiremock-spring-boot</artifactId>
                <version>${wiremock-spring-boot.version}</version>
            </dependency>
            <dependency>
                <groupId>dev.blaauwendraad</groupId>
                <artifactId>json-masker</artifactId>
                <version>${json-masker.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- ==================== Spring Boot Starters ==================== -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jdbc</artifactId>
        </dependency>

        <!-- ==================== 数据库驱动 ==================== -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- ==================== 工具库 ==================== -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>
        <dependency>
            <groupId>dev.blaauwendraad</groupId>
            <artifactId>json-masker</artifactId>
        </dependency>

        <!-- ==================== 测试依赖 ==================== -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.wiremock.integrations</groupId>
            <artifactId>wiremock-spring-boot</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Spring Boot Maven Plugin -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <!-- JaCoCo（详见第 2 节） -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>${jacoco.version}</version>
            </plugin>
        </plugins>
    </build>
</project>
```

### 1.3 依赖管理规则

#### 规则 1：BOM 管理的依赖不写版本号

```xml
<!-- ✅ 正确：BOM 已管理版本，无需指定 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
</dependency>

<!-- ❌ 错误：手动指定被 BOM 管理的版本，可能导致冲突 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.14.0</version>
</dependency>
```

#### 规则 2：非 BOM 依赖版本用 properties 管理

```xml
<!-- ✅ 正确：集中管理 -->
<properties>
    <json-masker.version>1.1.2</json-masker.version>
</properties>
<dependency>
    <groupId>dev.blaauwendraad</groupId>
    <artifactId>json-masker</artifactId>
    <version>${json-masker.version}</version>
</dependency>

<!-- ❌ 错误：硬编码版本 -->
<dependency>
    <groupId>dev.blaauwendraad</groupId>
    <artifactId>json-masker</artifactId>
    <version>1.1.2</version>
</dependency>
```

#### 规则 3：scope 必须显式声明

| scope | 使用场景 |
|-------|----------|
| `compile`（默认） | 主代码运行时依赖（可省略） |
| `provided` | 编译时需要但运行时由容器提供（Lombok、Jakarta Servlet API） |
| `runtime` | 仅运行时需要（JDBC 驱动、日志实现） |
| `test` | 仅测试时需要（JUnit、Mockito、H2、WireMock） |

#### 规则 4：`dependencyManagement` 仅用于版本声明

```xml
<!-- ✅ 正确：dependencyManagement 只声明版本，不引入依赖 -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.wiremock.integrations</groupId>
            <artifactId>wiremock-spring-boot</artifactId>
            <version>${wiremock-spring-boot.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- 实际引入在 <dependencies> 中，无需再写 version -->
<dependencies>
    <dependency>
        <groupId>org.wiremock.integrations</groupId>
        <artifactId>wiremock-spring-boot</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

> **注意：** 如果一个非 BOM 依赖只在单一 scope 使用（如仅 test），可以直接在 `<dependencies>` 中写 version，省去 `dependencyManagement` 的间接层。只有当同一 artifact 被 multiple scope 使用时，才需在 `dependencyManagement` 中统一版本。

### 1.4 版本冲突排查

```bash
# 查看依赖树（检测冲突）
mvn dependency:tree

# 查看特定依赖的来源
mvn dependency:tree -Dincludes=com.fasterxml.jackson.core

# 分析未使用的依赖
mvn dependency:analyze
```

---

## 2. JaCoCo 测试覆盖率

### 2.1 完整配置

```xml
<properties>
    <jacoco.version>0.8.12</jacoco.version>
    <!-- 覆盖率阈值 -->
    <jacoco.line-coverage>0.80</jacoco.line-coverage>
    <jacoco.branch-coverage>0.80</jacoco.branch-coverage>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>${jacoco.version}</version>
            <executions>
                <!-- 1. 准备 Agent：在 JVM 启动前注入 JaCoCo runtime -->
                <execution>
                    <id>prepare-agent</id>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>

                <!-- 2. 生成报告：测试完成后生成 HTML/XML 报告 -->
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>

                <!-- 3. 覆盖率检查：不达标则构建失败 -->
                <execution>
                    <id>check</id>
                    <phase>verify</phase>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>BUNDLE</element>
                                <limits>
                                    <limit>
                                        <counter>LINE</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>${jacoco.line-coverage}</minimum>
                                    </limit>
                                    <limit>
                                        <counter>BRANCH</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>${jacoco.branch-coverage}</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 2.2 覆盖率排除配置

排除不应计入覆盖率的代码（配置类、Lombok 生成代码等）：

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>${jacoco.version}</version>
    <configuration>
        <excludes>
            <!-- 应用启动类 -->
            <exclude>**/Application.class</exclude>

            <!-- 配置类（Spring @Configuration） -->
            <exclude>**/config/**</exclude>

            <!-- DTO / VO / Request / Response（纯数据载体无需测试） -->
            <exclude>**/dto/**</exclude>
            <exclude>**/vo/**</exclude>
            <exclude>**/request/**</exclude>
            <exclude>**/response/**</exclude>

            <!-- 枚举常量类 -->
            <exclude>**/enums/**</exclude>

            <!-- 异常类 -->
            <exclude>**/exception/**</exclude>
        </excludes>
    </configuration>
    <!-- ... executions 同上 ... -->
</plugin>
```

### 2.3 Lombok 生成代码排除

在 `lombok.config` 中配置，让 JaCoCo 忽略 Lombok 生成的方法：

```properties
# lombok.config — 放在项目根目录
lombok.addLombokGeneratedAnnotation = true
```

JaCoCo 0.8.0+ 会自动识别 `@Generated` 注解并跳过。

### 2.4 覆盖率阈值策略

| 阶段 | 行覆盖率 | 分支覆盖率 | 说明 |
|------|----------|-----------|------|
| 初始项目 | 0.50 | 0.40 | 允许较低的初始阈值 |
| 稳定开发 | 0.70 | 0.60 | 核心模块逐步提升 |
| 生产就绪 | **0.80** | **0.80** | 推荐的最终阈值 |

> **重要：** 覆盖率检查绑定到 `verify` 阶段，日常 `mvn test` 不受影响。CI 流水线使用 `mvn verify` 确保覆盖率达标。

### 2.5 常用命令

```bash
# 运行测试 + 生成报告
mvn test

# 查看报告位置
ls target/site/jacoco/index.html

# 运行覆盖率检查（不达标则失败）
mvn verify

# 只针对单个模块
mvn verify -pl my-module

# 跳过覆盖率检查（紧急情况）
mvn verify -Djacoco.skip=true
```

### 2.6 多模块项目配置

父 POM 在 `pluginManagement` 中声明配置，子模块按需继承：

```xml
<!-- 父 POM -->
<build>
    <pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>${jacoco.version}</version>
                <executions>
                    <!-- ... 同单模块配置 ... -->
                </executions>
            </plugin>
        </plugins>
    </pluginManagement>
</build>

<!-- 子模块（按需覆盖排除规则） -->
<build>
    <plugins>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>**/dto/**</exclude>
                </excludes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## 3. API 版本定制化

### 3.1 方案概述

| 方案 | 适用场景 | 实现方式 |
|------|----------|----------|
| **属性文件过滤** | 在 `/api/v1/` 路径中引用版本号 | Maven resource filtering |
| **构建信息注入** | 通过 `/actuator/info` 暴露版本 | `spring-boot-maven-plugin` build-info |
| **自定义 Header** | 响应头返回 API 版本 | Filter + properties |
| **URL 路径版本** | `/api/v{version}/resource` | Controller `@RequestMapping` 变量 |

### 3.2 方案 A：属性文件过滤（推荐用于 URL 版本）

**Step 1** — POM 中定义版本 property 并开启资源过滤：

```xml
<properties>
    <api.version>v1</api.version>
</properties>

<build>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
            <includes>
                <include>**/application*.yml</include>
                <include>**/application*.yaml</include>
                <include>**/application*.properties</include>
            </includes>
        </resource>
        <!-- 非过滤资源（二进制文件等） -->
        <resource>
            <directory>src/main/resources</directory>
            <filtering>false</filtering>
            <excludes>
                <exclude>**/application*.yml</exclude>
                <exclude>**/application*.yaml</exclude>
                <exclude>**/application*.properties</exclude>
            </excludes>
        </resource>
    </resources>
</build>
```

**Step 2** — `application.yml` 中引用：

```yaml
api:
  version: ${api.version}
  base-path: /api/${api.version}
```

**Step 3** — Controller 中使用：

```java
@RestController
@RequestMapping("${api.base-path}/projects")
public class ProjectController {
    // 实际路径: /api/v1/projects
}
```

**升级版本** — 仅需修改 POM property：

```xml
<properties>
    <api.version>v2</api.version>
</properties>
```

或通过命令行覆盖：

```bash
mvn spring-boot:run -Dapi.version=v2
```

### 3.3 方案 B：构建信息注入（Actuator）

无需额外配置，利用 `spring-boot-maven-plugin` 生成构建元数据：

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>build-info</goal>
            </goals>
            <configuration>
                <additionalProperties>
                    <api.version>${api.version}</api.version>
                    <java.version>${java.version}</java.version>
                </additionalProperties>
            </configuration>
        </execution>
    </executions>
</plugin>
```

在代码中读取：

```java
@Autowired
private BuildProperties buildProperties;

public String getApiVersion() {
    return buildProperties.get("api.version");
}
```

访问 `/actuator/info` 查看：

```json
{
  "build": {
    "version": "1.0.0",
    "api.version": "v1",
    "java.version": "21"
  }
}
```

> **前提：** 需引入 `spring-boot-starter-actuator` 并配置 `management.info.env.enabled=true`。

### 3.4 方案 C：自定义响应头

通过 Filter 在每个 API 响应中注入版本头：

```java
@Component
public class ApiVersionFilter implements Filter {

    @Value("${api.version:v1}")
    private String apiVersion;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse httpResponse) {
            httpResponse.setHeader("X-API-Version", apiVersion);
        }
        chain.doFilter(request, response);
    }
}
```

### 3.5 方案对比与推荐

```
                       方案A          方案B           方案C
                    属性文件过滤    构建信息注入    自定义响应头
─────────────────────────────────────────────────────────
URL 路径版本化         ✅              ❌             ❌
运行时版本可见          ✅              ✅             ✅
无需代码改动换版本       ✅              ❌             ❌
Actuator 集成          ❌              ✅             ❌
客户端版本协商          ❌              ❌             ✅
─────────────────────────────────────────────────────────
推荐场景             URL版本化      运维监控/Info    API Gateway

推荐组合：A + B（URL 版本化 + 构建信息注入）
```

### 3.6 Profile 级别的版本切换

通过 Maven Profile 为不同环境配置不同 API 版本：

```xml
<profiles>
    <profile>
        <id>dev</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <api.version>v1</api.version>
        </properties>
    </profile>
    <profile>
        <id>staging</id>
        <properties>
            <api.version>v2</api.version>
        </properties>
    </profile>
</profiles>
```

```bash
# 开发环境（默认 v1）
mvn spring-boot:run

# 预发环境（v2）
mvn spring-boot:run -Pstaging
```

---

## 4. 完整 POM 模板（合并三部分）

以下是整合了依赖管理优化 + JaCoCo + API 版本化的完整 POM 参考：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.11</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>my-service</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <!-- ==================== Properties ==================== -->
    <properties>
        <!-- 基础 -->
        <java.version>21</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <!-- API 版本 -->
        <api.version>v1</api.version>

        <!-- 第三方依赖版本 -->
        <json-masker.version>1.1.2</json-masker.version>
        <wiremock-spring-boot.version>4.0.9</wiremock-spring-boot.version>

        <!-- 插件版本 -->
        <jacoco.version>0.8.12</jacoco.version>

        <!-- JaCoCo 覆盖率阈值 -->
        <jacoco.line-coverage>0.80</jacoco.line-coverage>
        <jacoco.branch-coverage>0.80</jacoco.branch-coverage>
    </properties>

    <!-- ==================== Dependency Management ==================== -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>dev.blaauwendraad</groupId>
                <artifactId>json-masker</artifactId>
                <version>${json-masker.version}</version>
            </dependency>
            <dependency>
                <groupId>org.wiremock.integrations</groupId>
                <artifactId>wiremock-spring-boot</artifactId>
                <version>${wiremock-spring-boot.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- ==================== Dependencies ==================== -->
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- 数据库 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- 工具库 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>
        <dependency>
            <groupId>dev.blaauwendraad</groupId>
            <artifactId>json-masker</artifactId>
        </dependency>

        <!-- 测试 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.wiremock.integrations</groupId>
            <artifactId>wiremock-spring-boot</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <!-- ==================== Build ==================== -->
    <build>
        <!-- 资源过滤（支持 API 版本注入） -->
        <resources>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>true</filtering>
                <includes>
                    <include>**/application*.yml</include>
                    <include>**/application*.yaml</include>
                    <include>**/application*.properties</include>
                </includes>
            </resource>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>false</filtering>
                <excludes>
                    <exclude>**/application*.yml</exclude>
                    <exclude>**/application*.yaml</exclude>
                    <exclude>**/application*.properties</exclude>
                </excludes>
            </resource>
        </resources>

        <plugins>
            <!-- Spring Boot -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>build-info</goal>
                        </goals>
                        <configuration>
                            <additionalProperties>
                                <api.version>${api.version}</api.version>
                            </additionalProperties>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- JaCoCo -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>${jacoco.version}</version>
                <configuration>
                    <excludes>
                        <exclude>**/Application.class</exclude>
                        <exclude>**/config/**</exclude>
                        <exclude>**/dto/**</exclude>
                        <exclude>**/vo/**</exclude>
                        <exclude>**/enums/**</exclude>
                        <exclude>**/exception/**</exclude>
                    </excludes>
                </configuration>
                <executions>
                    <execution>
                        <id>prepare-agent</id>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>check</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>check</goal>
                        </goals>
                        <configuration>
                            <rules>
                                <rule>
                                    <element>BUNDLE</element>
                                    <limits>
                                        <limit>
                                            <counter>LINE</counter>
                                            <value>COVEREDRATIO</value>
                                            <minimum>${jacoco.line-coverage}</minimum>
                                        </limit>
                                        <limit>
                                            <counter>BRANCH</counter>
                                            <value>COVEREDRATIO</value>
                                            <minimum>${jacoco.branch-coverage}</minimum>
                                        </limit>
                                    </limits>
                                </rule>
                            </rules>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <!-- ==================== Profiles ==================== -->
    <profiles>
        <profile>
            <id>dev</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <api.version>v1</api.version>
            </properties>
        </profile>
        <profile>
            <id>v2</id>
            <properties>
                <api.version>v2</api.version>
            </properties>
        </profile>
    </profiles>
</project>
```

---

## 5. 审查清单

### POM 审查

- [ ] 是否继承 `spring-boot-starter-parent`？
- [ ] 非标准依赖版本是否集中在 `<properties>` 中？
- [ ] BOM 已管理的依赖是否省略了 `<version>`？
- [ ] 每个依赖是否声明了正确的 `<scope>`？
- [ ] `<dependencyManagement>` 中是否只声明版本，不含多余 scope？

### JaCoCo 审查

- [ ] 是否配置了 `prepare-agent` + `report` + `check` 三个 execution？
- [ ] 是否排除了 DTO / Config / Enum 等非业务代码？
- [ ] 是否配置了 `lombok.addLombokGeneratedAnnotation = true`？
- [ ] 覆盖率阈值是否通过 properties 可配置？
- [ ] CI 是否使用 `mvn verify` 而非 `mvn test`？

### API 版本审查

- [ ] 是否通过 properties 集中管理版本号？
- [ ] `application.yml` 是否引用 `${api.version}`？
- [ ] 资源过滤是否仅对配置文件开启（排除二进制文件）？
- [ ] 是否通过 `build-info` 暴露版本信息？
- [ ] 不同环境是否通过 Profile 区分版本？

---

## 附录 A：常用命令速查

```bash
# 编译
JAVA_HOME=/path/to/jdk21 mvn compile

# 运行全部测试 + 生成覆盖率报告
JAVA_HOME=/path/to/jdk21 mvn test

# 查看覆盖率报告
open target/site/jacoco/index.html

# 覆盖率检查（CI 用）
JAVA_HOME=/path/to/jdk21 mvn verify

# 查看依赖树
mvn dependency:tree

# 分析未使用依赖
mvn dependency:analyze

# 使用 v2 profile 运行
mvn spring-boot:run -Pv2

# 跳过测试打包
mvn package -DskipTests

# 查看生效的 POM（解析所有继承和 Profile）
mvn help:effective-pom
```

## 附录 B：版本号对照表

| 组件 | 推荐版本 | 说明 |
|------|----------|------|
| Spring Boot | 3.5.x | 当前最新稳定线 |
| JaCoCo Maven Plugin | 0.8.12 | 支持 JDK 21+ |
| WireMock Spring Boot | 4.0.9 | Spring Boot 3 兼容 |
| Lombok | 由 BOM 管理 | 无需手动指定 |
| commons-lang3 | 由 BOM 管理 | 无需手动指定 |
| json-masker | 1.1.2 | 非标准依赖 |

---

*规范结束*
