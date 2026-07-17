<p align="center">
  <img src="statics/img/logo.svg" alt="Dynamic Log Logo" width="120"/>
</p>
<h1 align="center">Dynamic Log</h1>
<p align="center">
  <strong>动态日志级别管理框架 - 运行时刷新日志级别，无需重启应用</strong>
</p>
<p align="center">
    <a target="_blank" href="https://search.maven.org/artifact/io.github.xiangganluo/dynamic-log-spring">
        <img src="https://img.shields.io/maven-central/v/io.github.xiangganluo/dynamic-log-spring.svg?label=Maven%20Central" />
    </a>
    <a target="_blank" href='https://www.apache.org/licenses/LICENSE-2.0.html'>
        <img src='https://img.shields.io/badge/license-Apache%202.0-blue.svg'/>
    </a>
    <a target="_blank" href="https://github.com/xiangganLuo/dynamic-log">
        <img src="https://img.shields.io/github/stars/xiangganLuo/dynamic-log.svg?style=social" alt="github star"/>
    </a>
    <img src="https://img.shields.io/badge/JDK-1.8%2B-green.svg"/>
</p>

---

## 📚 简介

Dynamic Log 是一款面向 Java 应用的动态日志级别管理框架，专为"运行时日志级别调整、配置中心集成、多日志框架适配"而设计。支持从 Apollo、Nacos 等配置中心实时刷新日志级别，无需重启应用，极大提升了线上问题排查效率。

核心与 Spring Boot 接入保持纯净，配置中心与增强能力都拆分为**按需引入的独立模块**（Apollo、Nacos、Log4j2、TTL、REST 端点、审计），用多少引多少。

**核心特性：**
- 🚀 **动态日志级别刷新** - 通过配置中心在运行时更新日志级别，无需重启应用
- 🎯 **Apollo / Nacos 开箱即用** - 各为独立模块，按需引入即自动注册监听器，无需手写监听逻辑
- 🔌 **多日志框架支持** - 可插拔的适配器架构，已提供 Logback 与 Log4j2 官方适配器，可平滑扩展到其他日志系统
- ⏳ **临时调级自动回滚** - TTL 插件临时调高级别、到期自动恢复，命中"排查完自动善后"
- 🛠️ **REST 端点** - 通过 HTTP 查询/设置日志级别（支持临时级别）
- 📝 **审计插件** - 把每次级别变更写入专用审计 logger，便于合规追溯
- 📊 **事件驱动架构** - 内置事件总线，支持日志级别变更通知和扩展
- 🧩 **插件系统** - 基于 SPI 的插件架构，无需修改核心代码即可自定义扩展
- ⚡ **Spring Boot 真开箱即用** - 自动注册 Logback 适配器、additive 应用预置级别，零配置集成
- ☕ **JDK 1.8 兼容** - 支持 Java 8 及以上版本

**使用场景：**

- **线上问题排查**：临时调高指定包的日志级别，获取详细日志后快速恢复（可用 TTL 插件到期自动回滚）
- **性能优化**：在生产环境动态降低非关键日志级别，减少日志输出对性能的影响
- **多环境管理**：不同环境使用不同的日志级别策略，通过配置中心统一管理
- **灰度发布**：在灰度期间临时开启详细日志，观察新版本行为
- **安全审计**：动态开启敏感操作的审计日志，配合审计插件满足合规要求

---

## 🏗️ 模块结构

```
Dynamic Log/
├── dynamic-log-dependencies-bom/   # 依赖版本BOM管理模块，统一管理所有依赖版本
├── dynamic-log-common/             # 公共模块 - 注解、异常、常量
├── dynamic-log-core/               # 核心模块 - 核心功能实现，不依赖Spring
├── dynamic-log-spring/             # Spring Boot自动配置 & Logback 适配器 & 刷新器
├── dynamic-log-apollo/             # Apollo 配置中心接入（可选）
├── dynamic-log-nacos/              # Nacos 配置中心接入（可选，面向 Spring Cloud Alibaba）
├── dynamic-log-log4j2/             # Log4j2 日志系统适配器（可选）
├── dynamic-log-plugin-ttl/         # 临时调级 + TTL 自动回滚（可选）
├── dynamic-log-endpoint/           # 运行期查询/设置级别的 REST 端点（可选）
├── dynamic-log-plugin-audit/       # 级别变更审计日志插件（可选）
├── dynamic-log-test/               # 测试模块 - 集成测试用例
└── dynamic-log-examples/           # 示例应用模块
```

| 模块名 | 说明 |
|--------|------|
| dynamic-log-dependencies-bom | 依赖版本BOM管理，所有依赖版本统一配置 |
| dynamic-log-common | 公共基础模块，注解、异常、常量 |
| dynamic-log-core | 核心实现模块，适配器、管理器、事件总线、插件、刷新器 |
| dynamic-log-spring | Spring Boot 自动配置、Logback 适配器、SpringEnvironmentRefresher |
| dynamic-log-apollo | Apollo 配置中心接入（可选） |
| dynamic-log-nacos | Nacos 配置中心接入（可选） |
| dynamic-log-log4j2 | Log4j2 日志系统适配器（可选） |
| dynamic-log-plugin-ttl | 临时调级 + TTL 到期自动回滚（可选） |
| dynamic-log-endpoint | 运行期查询/设置级别的 REST 端点（可选） |
| dynamic-log-plugin-audit | 级别变更审计日志插件（可选） |
| dynamic-log-test | 测试模块，集成测试用例 |
| dynamic-log-examples | 示例应用模块 |

---

## 📦 安装

### 🍊 Maven

在项目的 `pom.xml` 的 dependencies 中加入以下内容：

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>dynamic-log-spring</artifactId>
    <version>1.0.0</version>
</dependency>
```

> 推荐业务项目通过 BOM 方式统一依赖版本：
>
> ```xml
> <dependencyManagement>
>   <dependency>
>     <groupId>io.github.xiangganluo</groupId>
>     <artifactId>dynamic-log-dependencies-bom</artifactId>
>     <version>1.0.0</version>
>     <type>pom</type>
>     <scope>import</scope>
>   </dependency>
> </dependencyManagement>
> ```

---

## 🚀 快速开始

### 1. 依赖引入

在 Spring Boot 项目 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>dynamic-log-spring</artifactId>
</dependency>
```

引入后即触发自动配置，无需任何 `@EnableXxx` 注解——自动注册 Logback 适配器、创建管理器与刷新器。

### 2. 配置中心集成

配置中心接入为独立模块，按需引入即自动生效。

#### Apollo

引入 Apollo 模块 + Apollo 客户端：

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>dynamic-log-apollo</artifactId>
</dependency>
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
</dependency>
```

在 Apollo 配置中心添加日志级别配置：

```properties
logging.level.com.example=DEBUG
logging.level.com.example.service=INFO
```

模块会自动向 Apollo 默认命名空间（及 `dynamic-log.apollo.namespaces` 配置的额外命名空间）注册监听器，`logging.level.*` 变更实时生效，无需手动实现监听器。

#### Nacos

引入 Nacos 模块 + Spring Cloud Alibaba Nacos Config：

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>dynamic-log-nacos</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

在 Nacos 配置中心添加日志级别配置：

```yaml
logging:
  level:
    com.example: DEBUG
    com.example.service: WARN
```

模块监听 Spring Cloud 的 `EnvironmentChangeEvent`，在 Nacos 配置变更导致 `logging.level.*` 更新时自动刷新并应用，无需自行编写监听器。

### 3. 应用配置

```yaml
dynamic-log:
  enabled: true                    # 启用/禁用框架（默认：true）
  banner-enabled: true             # 启动时显示横幅（默认：true）
  default-adapter: logback         # 默认日志适配器（默认：logback）
  levels:                          # 可选：预定义日志级别（启动时 additive 应用，不覆盖 Boot 的 logging.level.*）
    com.example: DEBUG
    com.example.service: INFO
```

### 4. 编程式使用（不依赖 Spring）

```java
DynamicLogManager manager = DynamicLog.builder()
    .adapter(new LogbackSpringAdapter(loggingSystem))
    .defaultAdapter("logback")
    .build();

// 应用日志级别变更
LogLevelChange change = LogLevelChange.builder()
    .putLevel("com.example", "DEBUG")
    .build();
manager.applyLogLevelChange(change);
```

---

## 🔧 核心功能

### 适配器（策略模式）

`LoggingSystemAdapter` 接口抽象了不同的日志系统：

```java
public interface LoggingSystemAdapter {
    String getName();
    void setLogLevel(String loggerName, String level);
    String getLogLevel(String loggerName);
    Collection<String> getLoggerNames();
    void resetLogLevel(String loggerName);
}
```

官方适配器：
- `LogbackSpringAdapter`（`dynamic-log-spring`）- 适配 Logback，Spring 环境下自动注册
- `Log4j2LoggingSystemAdapter`（`dynamic-log-log4j2`）- 适配 Log4j2，引入模块即可用

### 事件总线（观察者模式）

订阅日志级别变更事件：

```java
manager.getEventBus().subscribe(new LogEventListener() {
    @Override
    public void onEvent(LogEvent event) {
        if (event.getType() == EventType.LOG_LEVEL_CHANGED) {
            // 处理日志级别变更
        }
    }
});
```

### 插件系统（SPI 模式）

通过自定义插件扩展框架：

```java
@Component
public class CustomPlugin implements DynamicLogPlugin {
    @Override
    public String getPluginId() { return "custom-plugin"; }

    @Override
    public int getOrder() { return 0; }

    @Override
    public void init(PluginContext context) {
        // 使用框架上下文初始化
    }

    @Override
    public void start() { /* 启动插件 */ }

    @Override
    public void stop() { /* 停止插件 */ }

    @Override
    public void destroy() { /* 清理资源 */ }
}
```

插件通过 Spring Bean 自动注册，生命周期由框架管理。

---

## 🧰 官方可选模块

以下模块均为 opt-in：引入即启用，`dynamic-log.<name>.enabled=false` 可关闭。

### Log4j2 适配器（dynamic-log-log4j2）

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>dynamic-log-log4j2</artifactId>
</dependency>
```

引入后自动注册 `Log4j2LoggingSystemAdapter`（名 `log4j2`），设 `dynamic-log.default-adapter: log4j2` 选用。

### 临时调级 · 自动回滚（dynamic-log-plugin-ttl）

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>dynamic-log-plugin-ttl</artifactId>
</dependency>
```

注入 `TemporaryLogLevelManager`，临时调级、到期自动回滚到原级别：

```java
@Autowired
private TemporaryLogLevelManager ttl;

// 把 com.example 临时调到 DEBUG，10 分钟后自动回滚
ttl.applyTemporary("com.example", "DEBUG", 10 * 60 * 1000L);
```

### REST 端点（dynamic-log-endpoint）

需要 Spring Web。提供两个接口：

```bash
# 查询所有 logger 当前级别
curl http://localhost:8080/dynamic-log/levels

# 设置级别（ttlSeconds 可选：正数=临时，到期自动回滚）
curl -X POST http://localhost:8080/dynamic-log/level \
     -H "Content-Type: application/json" \
     -d '{"logger":"com.example","level":"DEBUG","ttlSeconds":300}'
```

> ⚠️ 该端点能修改运行期日志级别，生产环境请置于内网或配合鉴权后暴露。

### 审计插件（dynamic-log-plugin-audit）

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>dynamic-log-plugin-audit</artifactId>
</dependency>
```

引入即订阅级别变更事件，把变更明细写入专用审计 logger（默认 `DYNAMIC-LOG-AUDIT`），可为其单独配置输出目的地做合规追溯。

---

## 🎯 扩展点

### 1. 自定义日志适配器

实现 `LoggingSystemAdapter` 接口以支持其他日志框架（Logback、Log4j2 已由官方模块提供）：

```java
@Component
public class MyLoggingSystemAdapter implements LoggingSystemAdapter {
    @Override
    public String getName() { return "my-logging"; }

    @Override
    public void setLogLevel(String loggerName, String level) {
        // 调用目标日志系统 API 设置级别；level 为 null 表示复位
    }
    // ... 其他方法
}
```

Spring 环境下暴露为 Bean，即被注册表的 `ObjectProvider` 自动收集。

### 2. 自定义配置监听器

参照 Nacos 模块思路，监听 `EnvironmentChangeEvent` 并触发刷新（适用于其他会刷新 Spring Environment 的配置源）：

```java
@Component
public class CustomConfigRefreshListener {
    @Autowired
    private SpringEnvironmentRefresher refresher;

    @EventListener
    public void onEnvironmentChange(EnvironmentChangeEvent event) {
        if (event.getKeys().stream().anyMatch(k -> k.startsWith("logging.level"))) {
            refresher.refresh();
        }
    }
}
```

### 3. 自定义插件

实现 `DynamicLogPlugin` 接口扩展框架功能：

```java
@Component
public class MetricsPlugin implements DynamicLogPlugin {
    @Override
    public String getPluginId() { return "metrics-plugin"; }

    @Override
    public void init(PluginContext context) {
        context.getEventBus().subscribe(event -> {
            // 收集日志级别变更指标
        });
    }
    // ... 其他方法
}
```

### 4. 自定义事件监听器

监听日志级别变更事件：

```java
@Component
public class AuditLogListener implements LogEventListener {
    @Override
    public void onEvent(LogEvent event) {
        if (event.getType() == EventType.LOG_LEVEL_CHANGED) {
            // 记录审计日志
        }
    }
}
```

---

## ⚙️ 配置项说明

**核心（前缀 `dynamic-log`）：**

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| dynamic-log.enabled | boolean | true | 是否启用 Dynamic Log 框架 |
| dynamic-log.banner-enabled | boolean | true | 启动时是否显示横幅 |
| dynamic-log.default-adapter | string | logback | 默认日志适配器名称 |
| dynamic-log.levels | Map | - | 预定义日志级别（启动时 additive 应用） |

**官方模块开关：**

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| dynamic-log.apollo.enabled | boolean | true | 是否启用 Apollo 集成 |
| dynamic-log.apollo.namespaces | List | [application] | Apollo 额外监听的命名空间 |
| dynamic-log.nacos.enabled | boolean | true | 是否启用 Nacos 集成 |
| dynamic-log.log4j2.enabled | boolean | true | 是否启用 Log4j2 适配器 |
| dynamic-log.ttl.enabled | boolean | true | 是否启用 TTL 临时调级管理器 |
| dynamic-log.endpoint.enabled | boolean | true | 是否启用 REST 端点 |
| dynamic-log.audit.enabled | boolean | true | 是否启用审计插件 |
| dynamic-log.audit.logger-name | string | DYNAMIC-LOG-AUDIT | 审计正文专用 logger 名 |
| dynamic-log.audit.audit-error | boolean | true | 是否同时审计级别变更失败事件 |

> 以上配置可在 application.yml 或 application.properties 中灵活配置。各模块开关仅在对应模块被引入后才有意义。

---

## 🎨 设计模式

| 模式 | 应用位置 |
|------|----------|
| **策略模式** | `LoggingSystemAdapter` 接口及多实现 |
| **建造者模式** | `DynamicLog.builder()`, `LogLevelChange.builder()` |
| **观察者/事件总线** | `LogEventBus` + `LogEventListener` 变更通知 |
| **插件/SPI** | `DynamicLogPlugin` + `PluginManager` 扩展机制 |
| **外观模式** | `DynamicLogManager` 统一入口 |
| **注册表模式** | `LoggingSystemAdapterRegistry` 适配器管理 |
| **模板方法** | `LoggingSystemAdapter` 中的默认方法 |

---

## 🤝 贡献

如果您觉得 Dynamic Log 有优化空间或有更好的设计思路，欢迎随时提交 PR（Pull Request）！我们鼓励社区共同完善和壮大本项目。

### 🐾 贡献代码的步骤
1. 在 GitHub 上 fork 本项目到您的个人仓库。
2. 将 fork 后的项目（即您的仓库）clone 到本地。
3. 在本地新建分支进行代码修改和优化。
4. commit 并 push 到您的远程仓库。
5. 登录 GitHub，在您的仓库首页点击 "Pull Request" 按钮，填写说明信息后提交。
6. 等待维护者 review 并合并。

### 📐 PR 遵循的原则

欢迎任何人为 Dynamic Log 添砖加瓦，贡献代码。

- **注释完备**：每个新增方法请按照 JavaDoc 规范标明方法说明、参数说明、返回值说明等，必要时请添加单元测试。
- **依赖规范**：新增方法尽量避免引入额外的第三方库。
- **风格统一**：请遵循项目现有代码风格和格式。

---

## 联系

如有问题或需要支持，扫码加微信(备注 dynamic-log)
<p>
<img src="statics/img/weixin.png" alt="微信二维码" width="230px"/>
</p>

---

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

---

## 致谢

- [Spring Boot](https://spring.io/projects/spring-boot) - 自动配置框架
- [Logback](https://logback.qos.ch/) - 默认日志系统适配器
- [Log4j2](https://logging.apache.org/log4j/2.x/) - 可选日志系统适配器
- [Apollo](https://github.com/ctripframework/apollo) - 携程开源配置中心
- [Nacos](https://nacos.io/) - 阿里巴巴开源配置中心

---

<p align="center">
  <strong>让动态日志管理更简单、更灵活！</strong>
</p>
