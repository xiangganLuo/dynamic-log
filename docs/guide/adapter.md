# 日志系统适配器

适配器（`LoggingSystemAdapter`）是 Dynamic Log 屏蔽底层日志系统差异的关键。无论应用用的是 Logback 还是 Log4j2，框架都通过同一套接口读写日志级别——这就是它「多框架支持、可插拔」的来源。

## LoggingSystemAdapter 接口

适配器采用策略模式，抽象了「读写某个 logger 级别」这一最小能力：

```java
public interface LoggingSystemAdapter {
    // 适配器名称，如 "logback" / "log4j2"
    String getName();

    // 设置某个 logger 的级别；level 为 null 表示复位
    void setLogLevel(String loggerName, String level);

    // 读取某个 logger 的当前有效级别，未显式设置返回 null
    String getLogLevel(String loggerName);

    // 返回当前所有已配置的 logger 名称
    Collection<String> getLoggerNames();

    // 复位某个 logger，使其重新继承父级级别
    void resetLogLevel(String loggerName);

    // 批量应用（默认逐条 setLogLevel）
    default void applyLevels(Map<String, String> levelMap) {
        levelMap.forEach(this::setLogLevel);
    }

    // 复位不在 exclude 集合内的 logger（排除 ROOT）
    default void resetAbsentLoggers(Collection<String> excludeLoggerNames) {
        getLoggerNames().stream()
                .filter(name -> !excludeLoggerNames.contains(name))
                .filter(name -> !"ROOT".equalsIgnoreCase(name))
                .forEach(this::resetLogLevel);
    }
}
```

两个 `default` 方法承载了框架「声明式应用」的语义：`applyLevels` 应用期望的级别集合，`resetAbsentLoggers` 把集合外的 logger 复位——两者配合即可保证每次刷新后的级别与配置中心完全一致。

## 内置：LogbackSpringAdapter

`dynamic-log-spring` 提供 `LogbackSpringAdapter`，基于 Spring Boot 的 `LoggingSystem` 抽象实现，天然兼容 Spring Boot 的日志基础设施：

```java
public class LogbackSpringAdapter implements LoggingSystemAdapter {
    public LogbackSpringAdapter(LoggingSystem loggingSystem) { ... }

    public String getName() { return "logback"; }
    // setLogLevel 内部转为 Spring 的 LogLevel 并委托 loggingSystem.setLogLevel
    // resetLogLevel 调用 loggingSystem.setLogLevel(name, null)
    // getLoggerNames / getLogLevel 读取 loggingSystem 的 LoggerConfiguration
}
```

要点：

- **复用 Spring Boot 抽象**：底层是 `org.springframework.boot.logging.LoggingSystem`，因此对 Logback、（以及 Spring Boot 支持的其他日志系统）都能工作。
- **非法级别安全跳过**：`setLogLevel` 遇到无法解析的级别字符串会告警并跳过，不会中断整批变更。
- **Spring Boot 环境自动装配**：当容器中没有其他 `LoggingSystemAdapter` Bean 时由自动配置注册（名 `logback`），无需手动 `new`。

## 官方：Log4j2 适配器

除 Logback 外，官方还提供 `dynamic-log-log4j2` 模块中的 `Log4j2LoggingSystemAdapter`（名 `log4j2`），基于 Log4j2 原生 `Configurator` 动态设置/重置级别。因此「多日志框架支持」不再是「可扩展」而是**已提供 Logback + Log4j2**。引入模块并把 `dynamic-log.default-adapter` 设为 `log4j2` 即可选用，详见 [官方模块与插件](/guide/plugins-official#log4j2-适配器)。

## 适配器注册表

多个适配器由 `LoggingSystemAdapterRegistry` 管理，`DefaultLoggingSystemAdapterRegistry` 是其线程安全实现（内部 `ConcurrentHashMap`）：

```java
public interface LoggingSystemAdapterRegistry {
    void register(LoggingSystemAdapter adapter);
    LoggingSystemAdapter unregister(String name);
    LoggingSystemAdapter getAdapter(String name);       // 不存在抛 AdapterNotFoundException
    LoggingSystemAdapter getDefaultAdapter();
    void setDefaultAdapter(String name);
    Collection<String> getRegisteredNames();
    boolean contains(String name);
}
```

行为要点：

- **首个即默认**：第一个注册的适配器自动成为默认适配器，之后可用 `setDefaultAdapter(name)` 覆盖。
- **禁止重名**：注册同名适配器会抛 `IllegalArgumentException`。
- **未知名报错**：`getAdapter` / `setDefaultAdapter` 遇到未注册名抛 `AdapterNotFoundException`。

## 自定义适配器

Logback 与 Log4j2 已由官方模块覆盖；如需适配其他日志系统（如 JUL），实现 `LoggingSystemAdapter` 并注册即可：

```java
public class MyLoggingSystemAdapter implements LoggingSystemAdapter {
    @Override public String getName() { return "my-logging"; }

    @Override public void setLogLevel(String loggerName, String level) {
        // 调用目标日志系统的 API 设置级别；level 为 null 表示复位
    }

    @Override public String getLogLevel(String loggerName) { /* ... */ return null; }

    @Override public Collection<String> getLoggerNames() { /* ... */ return java.util.Collections.emptyList(); }

    @Override public void resetLogLevel(String loggerName) {
        setLogLevel(loggerName, null); // 或按需还原为父级
    }
}
```

注册方式二选一：

```java
// 方式一：编程式，通过构建器注册
DynamicLogManager manager = DynamicLog.builder()
        .adapter(new MyLoggingSystemAdapter())
        .defaultAdapter("my-logging")
        .build();
```

```java
// 方式二：Spring Boot 下暴露为 Bean，由注册表的 ObjectProvider 自动收集
@Bean
public LoggingSystemAdapter myLoggingSystemAdapter() {
    return new MyLoggingSystemAdapter();
}
```

::: tip 指定适配器应用
需要向非默认适配器应用变更时，用带适配器名的重载：`manager.applyLogLevelChange(change, "log4j2")`。
:::

## 下一步

- [核心概念](/guide/concepts)：管理器如何驱动适配器。
- [事件体系](/guide/events)：适配器注册与级别变更事件。
- [动态刷新与配置中心](/guide/refresh)：刷新器如何把配置转成级别变更。
