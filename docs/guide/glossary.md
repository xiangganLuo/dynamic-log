# 术语表

Dynamic Log 常用术语的简明定义，便于统一理解。更完整的说明见对应章节链接。

## 核心组件

- **DynamicLogManager** —— 框架统一入口（外观模式），协调适配器注册表、事件总线与插件管理器，提供 `applyLogLevelChange` / `resetAllLogLevels` 等操作。见 [核心概念](/guide/concepts#dynamiclogmanager-门面)。
- **DynamicLog（构建器）** —— 流畅的建造者，通过 `DynamicLog.builder()...build()` 装配适配器、事件总线、刷新器并产出 `DynamicLogManager`。
- **LogLevelChange** —— 一次日志级别变更的不可变载体（logger 名 → 级别），由 Builder 构造。见 [核心概念](/guide/concepts#loglevelchange-级别变更)。

## 适配器

- **LoggingSystemAdapter** —— 抽象具体日志系统读写级别能力的策略接口：`setLogLevel` / `getLogLevel` / `getLoggerNames` / `resetLogLevel`，以及默认的 `applyLevels` / `resetAbsentLoggers`。见 [日志系统适配器](/guide/adapter)。
- **LogbackSpringAdapter** —— 内置适配器，基于 Spring Boot `LoggingSystem` 抽象适配 Logback。
- **LoggingSystemAdapterRegistry** —— 适配器注册表；首个注册者默认为默认适配器，禁止重名。
- **resetAbsentLoggers** —— 复位不在本次变更集合内的 logger（ROOT 除外），保证最终级别与配置一致。

## 刷新与配置中心

- **LogRefresher** —— 检测并触发级别刷新的策略接口：`start` / `stop` / `refresh` / `isRunning`。见 [动态刷新与配置中心](/guide/refresh)。
- **SpringEnvironmentRefresher** —— 主刷新器（策略名 `spring-environment`，`dynamic-log-spring` 提供），从 Spring `Environment` 读取全部 `logging.level.*` 并整体应用。
- **ApolloLogLevelListener** —— `dynamic-log-apollo` 模块的 Apollo 原生监听器，筛出 `logging.level.*` 变更键并 `applyLogLevelChange`。
- **NacosLogLevelChangeListener** —— `dynamic-log-nacos` 模块，监听 Spring Cloud `EnvironmentChangeEvent`，命中日志键时触发 `SpringEnvironmentRefresher.refresh()`。
- **logging.level.\*** —— 沿用 Spring Boot 标准的日志级别配置键，Dynamic Log 直接读取。

## 官方插件与模块

- **Log4j2LoggingSystemAdapter** —— `dynamic-log-log4j2` 提供的 Log4j2 适配器（名 `log4j2`）。见 [官方模块与插件](/guide/plugins-official)。
- **TemporaryLogLevelManager** —— `dynamic-log-plugin-ttl` 提供，`applyTemporary(logger, level, ttlMillis)` 临时调级、到期自动回滚。
- **DynamicLogController** —— `dynamic-log-endpoint` 提供的 REST 端点（`GET /dynamic-log/levels`、`POST /dynamic-log/level`，支持 `ttlSeconds`）。
- **AuditLogPlugin** —— `dynamic-log-plugin-audit` 提供，订阅级别变更事件写入专用审计 logger（默认 `DYNAMIC-LOG-AUDIT`）。

## 事件

- **LogEventBus** —— 实例级事件总线，支持同步 / 异步发布与订阅。见 [事件体系](/guide/events)。
- **LogEvent** —— 不可变事件对象，含 `type`、`attributes`、`timestamp`，通过 Builder 构造。
- **EventType** —— 事件类型枚举：`LOG_LEVEL_CHANGE` / `LOG_LEVEL_CHANGED` / `REFRESHER_STARTED` / `REFRESHER_STOPPED` / `ADAPTER_REGISTERED` / `ADAPTER_UNREGISTERED` / `ERROR`。
- **LogEventListener** —— 事件监听器，`onEvent` 消费事件，`getOrder()` 控制执行顺序（越小越先）。

## 插件

- **DynamicLogPlugin** —— 以统一生命周期接入框架的扩展单元：`getPluginId()` + `init/start/stop/destroy`，可选 `getVersion()` / `getOrder()`。见 [插件系统](/guide/plugin)。
- **PluginContext** —— 插件访问内核的受控入口：`getAdapterRegistry` / `getEventBus` / `getLogManager` / `getProperty`。
- **PluginManager** —— 插件的注册、装配与启停：`register` / `unregister` / `startAll`（按序）/ `stopAll`（逆序）。
- **DynamicLogPluginLifecycle** —— Spring `SmartLifecycle` 实现，自动收集容器中所有 `DynamicLogPlugin` Bean 并随应用启停装配。

## 模块

- **dynamic-log-common** —— 公共基础：注解、异常（如 `AdapterNotFoundException`）、常量（如 `logging.level` 前缀）。
- **dynamic-log-core** —— 零 Spring 依赖的核心内核：适配器、管理器、事件总线、插件、刷新器抽象。
- **dynamic-log-spring** —— Spring Boot 自动配置、`LogbackSpringAdapter`、`SpringEnvironmentRefresher`（不含配置中心依赖）。
- **dynamic-log-apollo / dynamic-log-nacos** —— 可选配置中心接入模块。
- **dynamic-log-log4j2 / dynamic-log-plugin-ttl / dynamic-log-endpoint / dynamic-log-plugin-audit** —— 可选官方能力模块（适配器 / TTL / REST 端点 / 审计）。
- **dynamic-log-dependencies-bom** —— 依赖版本统一管理 BOM。
