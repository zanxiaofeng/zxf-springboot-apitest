# Java 契约编程规范（正式版）

**版本：** 1.0  
**生效日期：** 2026-03-23  
**适用范围：** 所有基于 Java 17+ 的服务端、客户端及基础库代码

---

## 1. 目的

1. 明确方法的前置条件（Preconditions）、后置条件（Postconditions）与类不变式（Class Invariants）。
2. 通过"快失败"（Fail Fast）原则，尽早暴露错误，降低调试成本。
3. 提高代码可读性，使方法的依赖与约束显式化，契约即文档。

---

## 2. 核心原则

### 2.1 契约即代码

方法的契约应当通过代码显式表达，而非仅依赖注释。任何开发者阅读代码时应能直接理解方法的约束条件。

### 2.2 区分输入校验与内部断言

| 场景 | 机制 | 用途 |
|------|------|------|
| 外部输入 | `Preconditions` / `Objects.requireNonNull` | 参数、配置、外部系统返回的校验 |
| 内部假设 | `assert` | 验证代码逻辑假设、不变式、后置条件 |

### 2.3 明确异常类型

违反契约应抛出**非受检异常**：

| 异常类型 | 使用场景 |
|----------|----------|
| `IllegalArgumentException` | 参数值不合法 |
| `IllegalStateException` | 对象状态不正确 |
| `NullPointerException` | 参数或状态为 null（优先使用 `Objects.requireNonNull`） |
| `IndexOutOfBoundsException` | 索引越界 |

### 2.4 信息充分原则

异常信息必须包含：
- 不合法的参数/状态名称
- 预期的约束条件
- 实际值（若安全且有助于排查）

---

## 3. 参数校验（前置条件）

### 3.1 强制校验规则

所有 `public` / `protected` 方法的所有参数，必须在方法入口处进行校验。

### 3.2 工具选择优先级

| 优先级 | 工具 | 适用场景 |
|--------|------|----------|
| 1 | `Objects.requireNonNull()` | JDK 原生，仅非空校验 |
| 2 | `Preconditions` (Guava) | 复杂校验场景（范围、条件等） |
| 3 | 自建工具类 | 项目未引入 Guava 时 |

### 3.3 基础校验类型

- 非空校验
- 数值范围校验
- 字符串非空/格式校验
- 集合非空校验
- 对象状态校验

### 3.4 正确示例

```java
import java.util.Objects;
import com.google.common.base.Preconditions;

public void updateUser(@Nonnull String userId, int age, List<String> tags) {
    // 非空校验
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(tags, "tags must not be null");
    
    // 字符串非空校验
    Preconditions.checkArgument(!userId.trim().isEmpty(), 
        "userId must not be blank");
    
    // 数值范围校验
    Preconditions.checkArgument(age >= 0 && age <= 150, 
        "age out of range [0, 150], was: %s", age);
    
    // 集合元素校验
    Preconditions.checkArgument(!tags.isEmpty(), 
        "tags must not be empty");
    
    // 业务逻辑...
}
```

### 3.5 错误示例

```java
public void updateUser(String userId, int age) {
    // ❌ 未进行参数校验
    // 若 userId 为 null 或 age 异常，后续可能产生难以追踪的异常
    userDao.update(userId, age);
}
```

### 3.6 私有方法校验

私有方法通常由信任的调用方使用，但若逻辑复杂或涉及外部数据，也应校验关键参数：

```java
private void calculateDiscount(BigDecimal amount, int level) {
    // 虽为私有方法，但 amount 可能来自外部数据
    Objects.requireNonNull(amount, "amount must not be null");
    Preconditions.checkArgument(amount.compareTo(BigDecimal.ZERO) >= 0,
        "amount must be non-negative, was: %s", amount);
    
    // 计算逻辑...
}
```

---

## 4. 内部断言（不变式与后置条件）

### 4.1 assert 的使用原则

`assert` 仅用于验证**代码内部逻辑假设**，不可用于外部输入校验。

**适用场景：**
- 方法执行后对象状态符合预期（后置条件）
- 算法过程中某些变量值应在预期范围内
- 类不变式（Class Invariants）

### 4.2 正确示例

```java
private void deductInventory(int quantity) {
    // 前置校验（外部输入）
    Preconditions.checkArgument(quantity > 0, 
        "quantity must be positive, was: %s", quantity);
    
    int beforeStock = this.stock;
    
    // 扣减逻辑...
    this.stock -= quantity;
    
    // 后置条件断言（内部假设）
    assert this.stock >= 0 : 
        "stock became negative after deduction";
    assert this.stock == beforeStock - quantity : 
        "stock calculation error";
}
```

### 4.3 断言开启配置

| 环境 | JVM 参数 | 说明 |
|------|----------|------|
| 开发/测试 | `-ea` (或 `-enableassertions`) | 开启断言 |
| 生产 | `-da` (或 `-disableassertions`) | 关闭断言（默认） |

**重要：** 断言失败应视为编程错误，**不应被 try-catch 捕获处理**。

---

## 5. 使用注解声明契约

### 5.1 标准注解

| 注解 | 来源 | 用途 |
|------|------|------|
| `@NotNull` / `@Nullable` | `javax.annotation` / `org.jetbrains.annotations` | 声明可空性 |
| `@Nonnull` / `@CheckForNull` | `javax.annotation` | JSR-305 标准 |
| `@NonNull` | `lombok` | Lombok 项目使用 |

### 5.2 注解使用示例

```java
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UserService {
    
    /**
     * 根据ID获取用户
     * @param id 用户ID，不可为空且长度至少为1
     * @return 用户对象，若不存在则返回null
     */
    public @Nullable User getById(@Nonnull String id) {
        Objects.requireNonNull(id, "id must not be null");
        Preconditions.checkArgument(!id.isEmpty(), 
            "id must not be empty");
        // ...
    }
    
    /**
     * 创建用户
     * @param user 用户对象，不可为空
     * @return 创建后的用户，永不为空
     */
    public @Nonnull User create(@Nonnull User user) {
        Objects.requireNonNull(user, "user must not be null");
        // ...
        User created = userRepository.save(user);
        assert created != null : "save() must not return null";
        return created;
    }
}
```

### 5.3 注解与代码一致性

**强制要求：** 校验逻辑必须与注解声明的契约保持一致。

```java
// ❌ 错误：声明 @Nonnull 却未校验
public void process(@Nonnull String input) {
    // 缺少校验，若 input 为 null 会抛出 NPE
    System.out.println(input.length());
}

// ✅ 正确：声明与校验一致
public void process(@Nonnull String input) {
    Objects.requireNonNull(input, "input must not be null");
    System.out.println(input.length());
}
```

---

## 6. 异常信息规范

### 6.1 信息格式标准

所有违反契约抛出的异常，信息格式如下：

```
[参数/状态名] must [约束条件], but was: [实际值]
```

### 6.2 正确示例

```java
// 数值范围
Preconditions.checkArgument(age >= 0 && age <= 150,
    "age must be in range [0, 150], but was: %s", age);

// 非空字符串
Preconditions.checkArgument(!name.trim().isEmpty(),
    "name must not be blank, but was: '%s'", name);

// 集合大小
Preconditions.checkArgument(items.size() <= MAX_SIZE,
    "items size must not exceed %s, but was: %s", MAX_SIZE, items.size());

// 状态检查
if (!isInitialized()) {
    throw new IllegalStateException(
        "service must be initialized before use, current state: UNINITIALIZED");
}
```

### 6.3 错误示例

```java
// ❌ 信息不完整
if (amount < 0) {
    throw new IllegalArgumentException("invalid amount");
}

// ❌ 未说明期望值
Preconditions.checkArgument(age > 0, "bad age: %s", age);

// ❌ 未包含实际值
Preconditions.checkNotNull(userId, "userId is required");
```

---

## 7. 契约与继承

### 7.1 里氏替换原则（LSP）

子类重写方法时：
- **不能放宽前置条件**（即允许更多非法输入）
- **不能加强后置条件**（即输出更严格的保证）
- **不能削弱类不变式**

```java
public abstract class PaymentProcessor {
    /**
     * 处理支付
     * @param amount 金额，必须为正数
     */
    public abstract void process(@Nonnull BigDecimal amount);
}

public class CreditCardProcessor extends PaymentProcessor {
    @Override
    public void process(@Nonnull BigDecimal amount) {
        // ✅ 正确：保持或加强前置条件
        Objects.requireNonNull(amount, "amount must not be null");
        Preconditions.checkArgument(amount.compareTo(BigDecimal.ZERO) > 0,
            "amount must be positive, was: %s", amount);
        
        // ❌ 错误：放宽前置条件（允许负数）
        // Preconditions.checkArgument(amount.compareTo(BigDecimal.ZERO) >= 0, ...);
        
        // 处理逻辑...
    }
}
```

### 7.2 模板方法模式

使用模板方法固化契约，确保子类遵循统一约束：

```java
public abstract class BaseProcessor {
    
    /**
     * 模板方法：统一处理参数校验与后置检查
     */
    public final @Nonnull String process(@Nonnull String input) {
        // 前置条件校验
        Objects.requireNonNull(input, "input must not be null");
        Preconditions.checkArgument(!input.isEmpty(),
            "input must not be empty");
        
        // 执行子类逻辑
        String result = doProcess(input);
        
        // 后置条件断言
        assert result != null : "doProcess() must not return null";
        assert !result.isEmpty() : "doProcess() must not return empty string";
        
        return result;
    }
    
    /**
     * 子类实现具体逻辑
     */
    protected abstract @Nonnull String doProcess(@Nonnull String input);
}
```

---

## 8. 类不变式（Class Invariants）

### 8.1 定义

类不变式是在对象整个生命周期中必须始终保持为真的条件。

### 8.2 实现方式

```java
public class BankAccount {
    private String accountId;
    private BigDecimal balance;
    
    public BankAccount(@Nonnull String accountId, @Nonnull BigDecimal initialBalance) {
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.balance = Objects.requireNonNull(initialBalance, "initialBalance must not be null");
        Preconditions.checkArgument(initialBalance.compareTo(BigDecimal.ZERO) >= 0,
            "initial balance must be non-negative, was: %s", initialBalance);
        
        // 构造后校验不变式
        assert invariant() : "class invariant violated after construction";
    }
    
    public void withdraw(@Nonnull BigDecimal amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        Preconditions.checkArgument(amount.compareTo(BigDecimal.ZERO) > 0,
            "amount must be positive, was: %s", amount);
        Preconditions.checkArgument(balance.compareTo(amount) >= 0,
            "insufficient balance: required %s, available %s", amount, balance);
        
        this.balance = this.balance.subtract(amount);
        
        // 方法执行后校验不变式
        assert invariant() : "class invariant violated after withdraw";
    }
    
    /**
     * 不变式检查
     */
    private boolean invariant() {
        return accountId != null && !accountId.isEmpty()
            && balance != null && balance.compareTo(BigDecimal.ZERO) >= 0;
    }
}
```

---

## 9. 测试契约

### 9.1 负面测试

为每个公开方法编写测试，验证非法输入抛出预期异常：

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    
    private final UserService service = new UserService();
    
    @Test
    void updateUser_NullUserId_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.updateUser(null, 25)
        );
        assertEquals("userId must not be null", exception.getMessage());
    }
    
    @ParameterizedTest
    @ValueSource(ints = {-1, 151, Integer.MIN_VALUE})
    void updateUser_InvalidAge_ShouldThrowException(int invalidAge) {
        assertThrows(IllegalArgumentException.class,
            () -> service.updateUser("user1", invalidAge));
    }
    
    @ParameterizedTest
    @CsvSource({
        "user1, -1, age out of range",
        "user1, 151, age out of range",
        "'', 25, userId must not be blank"
    })
    void updateUser_InvalidInputs_ShouldThrowException(
            String userId, int age, String expectedMessage) {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.updateUser(userId, age)
        );
        assertTrue(exception.getMessage().contains(expectedMessage));
    }
}
```

### 9.2 边界测试

```java
@ParameterizedTest
@ValueSource(strings = {"a", "ab", "user123"})
void updateUser_ValidUserId_ShouldSucceed(String userId) {
    assertDoesNotThrow(() -> service.updateUser(userId, 25));
}

@ParameterizedTest
@ValueSource(ints = {0, 1, 25, 149, 150})
void updateUser_ValidAge_ShouldSucceed(int age) {
    assertDoesNotThrow(() -> service.updateUser("user1", age));
}
```

---

## 10. 代码审查清单

- [ ] 所有 `public` / `protected` 方法是否都有参数校验？
- [ ] 异常信息是否清晰，包含参数名、期望值和实际值？
- [ ] 是否将 `assert` 误用于外部输入校验？
- [ ] 注解声明（`@NotNull` / `@Nullable`）是否与校验逻辑一致？
- [ ] 私有方法涉及外部数据时是否进行了关键参数校验？
- [ ] 子类重写方法是否遵循 LSP 原则？
- [ ] 类不变式是否在关键方法后得到维护？
- [ ] 测试是否覆盖了契约的边界情况？

---

## 附录 A：常用工具库

| 类型 | 推荐库 | 说明 |
|------|--------|------|
| 前置条件校验 | Guava `Preconditions` | 功能全面，推荐首选 |
| 前置条件校验 | Apache Commons Lang `Validate` | 备选方案 |
| 非空校验 | JDK `Objects.requireNonNull` | 零依赖，轻量级 |
| 注解 | `javax.annotation` (JSR-305) | 标准注解 |
| 注解 | `org.jetbrains.annotations` | IntelliJ 兼容 |
| 单元测试 | JUnit 5 | 现代测试框架 |
| 断言增强 | AssertJ | 流式断言 API |

---

## 附录 B：契约编程决策流程

```
开始编写方法
    │
    ▼
定义方法契约
    │
    ├── 参数约束？ ──► 使用 Preconditions / Objects 校验
    │
    ├── 返回值保证？ ──► 使用 assert 校验后置条件
    │
    ├── 类状态约束？ ──► 定义 invariant() 方法
    │
    └── 可空性？ ──► 使用 @NotNull / @Nullable 注解
    │
    ▼
实现业务逻辑
    │
    ▼
编写测试
    │
    ├── 正面测试（合法输入）
    │
    └── 负面测试（非法输入）
```

---

## 附录 C：快速参考卡片

### 参数校验模板

```java
// 非空
Objects.requireNonNull(param, "paramName must not be null");

// 字符串非空
Preconditions.checkArgument(!str.trim().isEmpty(), 
    "str must not be blank");

// 数值范围
Preconditions.checkArgument(value >= MIN && value <= MAX,
    "value must be in range [%s, %s], was: %s", MIN, MAX, value);

// 集合非空
Preconditions.checkArgument(!collection.isEmpty(),
    "collection must not be empty");

// 状态检查
Preconditions.checkState(isReady(), 
    "service must be ready, current state: %s", getState());
```

### 断言模板

```java
// 后置条件
assert result != null : "method must not return null";

// 不变式
assert invariant() : "class invariant violated";

// 内部假设
assert index >= 0 && index < size : "index out of bounds";
```

---

> **遵循本规范**，可使代码契约清晰可读、错误快速暴露，从而提升系统健壮性与团队协作效率。