# 官方模块与插件

除核心与 Spring Boot 接入外，Dynamic Log 还提供一批**可选官方模块**。它们都遵循「引入即启用、`dynamic-log.<name>.enabled=false` 可关闭」的约定（opt-in），互不强耦合，按需取用。

| 模块 | 作用 | 开关 |
|------|------|------|
| `dynamic-log-log4j2` | Log4j2 日志系统适配器 | `dynamic-log.log4j2.enabled` |
| `dynamic-log-plugin-ttl` | 临时调级 + TTL 到期自动回滚 | `dynamic-log.ttl.enabled` |
| `dynamic-log-endpoint` | 运行期查询/设置级别的 REST 端点 | `dynamic-log.endpoint.enabled` |
| `dynamic-log-plugin-audit` | 级别变更审计日志插件 | `dynamic-log.audit.enabled` |

各模块 Maven 坐标均为 `io.github.xiangganluo:<artifactId>:1.0.0`。

## Log4j2 适配器

模块 `dynamic-log-log4j2` 补齐「多日志框架支持」——在此之前只内置 Logback，现已提供官方 Log4j2 适配器。

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>dynamic-log-log4j2</artifactId>
    <version>1.0.0</version>
</dependency>
```

`Log4j2LoggingSystemAdapter`（适配器名 `log4j2`）基于 Log4j2 原生 API（`Configurator.setLevel` 动态设置/重置级别，从当前 `LoggerContext` 的 `Configuration` 收集已配置的 logger）。引入后它会被 `dynamic-log-spring` 的适配器注册收集器自动收进注册表，通过配置指定为默认适配器：

```yaml
dynamic-log:
  default-adapter: log4j2
```

- 生效条件：classpath 存在 Log4j2 核心（`org.apache.logging.log4j.core.LoggerContext`），且 `dynamic-log.log4j2.enabled` 未关闭。
- 使用 Log4j2 的应用请确保项目用 Log4j2 作为日志实现（排除 Spring Boot 默认的 Logback，改用 `spring-boot-starter-log4j2`）。

::: tip 默认适配器的取舍
`dynamic-log-spring` 仅当容器中没有其他 `LoggingSystemAdapter` Bean 时才注册默认的 Logback 适配器。引入 log4j2 模块后，注册表里就是 log4j2 适配器；若确需同时保留两者，可自行再声明一个 `LogbackSpringAdapter` Bean。
:::

## 临时调级与自动回滚

模块 `dynamic-log-plugin-ttl` 面向「线上排查完快速恢复」的痛点：临时把某个包调到 `DEBUG`，到点自动恢复原级别，避免忘记回滚导致日志爆量。

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>dynamic-log-plugin-ttl</artifactId>
    <version>1.0.0</version>
</dependency>
```

引入后自动装配一个 `TemporaryLogLevelManager` Bean（受 `dynamic-log.ttl.enabled` 控制，默认开），注入即用：

```java
@Autowired
private TemporaryLogLevelManager ttl;

public void debugFor10Minutes() {
    // 临时把 com.example 调到 DEBUG，10 分钟后自动回滚到原级别
    ttl.applyTemporary("com.example", "DEBUG", 10 * 60 * 1000L);
}
```

API：

```java
// 默认适配器
void applyTemporary(String loggerName, String level, long durationMillis);
// 指定适配器
void applyTemporary(String loggerName, String level, long durationMillis, String adapterName);
// 立即回滚并清除该 logger 的待回滚任务
void cancel(String loggerName);
```

语义要点：

- 应用前记录**原级别**（`null` 表示原本未显式设置、继承父级），到期后精确回滚（原级别非空→重设；为空→`resetLogLevel` 继承父级）。
- 对同一 logger 重复 `applyTemporary`：取消旧的待回滚任务，并**保留最初一次**记录的原级别（不会把「当前临时值」误当原值）。
- 内部为单线程 daemon 调度器；容器关闭时通过 `@Bean(destroyMethod="shutdown")` 释放（关闭时不再执行到期回滚，直接清理）。

## REST 端点

模块 `dynamic-log-endpoint` 通过 HTTP 在运行期查询/设置日志级别，`POST` 支持临时级别 + 到期自动回滚。需要 Spring Web。

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>dynamic-log-endpoint</artifactId>
    <version>1.0.0</version>
</dependency>
<!-- 若项目尚未引入 Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

生效条件：classpath 存在 Spring Web（`RestController`）、容器存在 `DynamicLogManager`，且 `dynamic-log.endpoint.enabled` 未关闭。

**查询所有 logger 的当前级别：**

```bash
curl http://localhost:8080/dynamic-log/levels
```

```json
{ "com.example": "DEBUG", "org.springframework": "INFO", "ROOT": "INFO" }
```

**设置某个 logger 的级别：**

```bash
# 永久设置
curl -X POST http://localhost:8080/dynamic-log/level \
     -H "Content-Type: application/json" \
     -d '{"logger":"com.example","level":"DEBUG"}'

# 临时设置：300 秒后自动回滚到设置前的级别
curl -X POST http://localhost:8080/dynamic-log/level \
     -H "Content-Type: application/json" \
     -d '{"logger":"com.example","level":"DEBUG","ttlSeconds":300}'
```

请求体字段：`logger`（必填）、`level`（必填，大小写不敏感，合法值 `TRACE/DEBUG/INFO/WARN/ERROR/FATAL/OFF`）、`ttlSeconds`（可选，为空或非正数=永久，正数=临时）。回滚由端点内自包含的守护调度器实现，对同一 logger 重复临时设置会取消旧任务并保留最初原级别；容器关闭时调度器随 `DisposableBean` 释放。

::: warning 生产环境请加访问控制
该端点能修改运行期日志级别，请置于内网或配合鉴权（如 Spring Security / 网关白名单）后再对外暴露。
:::

## 审计插件

模块 `dynamic-log-plugin-audit` 把每次日志级别变更「何时 / 改了哪些 logger → 什么级别 / 用哪个适配器」写入专用审计 logger，便于合规追溯。

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>dynamic-log-plugin-audit</artifactId>
    <version>1.0.0</version>
</dependency>
```

引入即装配一个 `AuditLogPlugin`（`DynamicLogPlugin`），由 `dynamic-log-spring` 的插件生命周期自动注册启动，订阅事件总线：默认审计 `LOG_LEVEL_CHANGED`（变更已生效），`audit-error=true` 时同时审计 `ERROR`（变更失败）。审计正文写入专用 logger（默认 `DYNAMIC-LOG-AUDIT`）。

配置项（前缀 `dynamic-log.audit`）：

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `dynamic-log.audit.enabled` | boolean | `true` | 是否启用审计插件 |
| `dynamic-log.audit.logger-name` | string | `DYNAMIC-LOG-AUDIT` | 审计正文专用 logger 名 |
| `dynamic-log.audit.audit-error` | boolean | `true` | 是否同时审计变更失败事件 |

可为审计 logger 单独配置输出目的地（如独立审计文件）。以 Logback 为例：

```xml
<appender name="AUDIT_FILE" class="ch.qos.logback.core.FileAppender">
    <file>logs/dynamic-log-audit.log</file>
    <encoder><pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %msg%n</pattern></encoder>
</appender>
<logger name="DYNAMIC-LOG-AUDIT" level="INFO" additivity="false">
    <appender-ref ref="AUDIT_FILE"/>
</logger>
```

## 下一步

- [日志系统适配器](/guide/adapter)：适配器接口与 Logback / Log4j2。
- [插件系统（Plugin SPI）](/guide/plugin)：审计插件遵循的插件模型。
- [动态刷新与配置中心](/guide/refresh)：Apollo / Nacos 独立模块。
