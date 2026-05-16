<p align="center">
  <img src="statics/img/logo.svg" alt="Dynamic Log Logo" width="120"/>
</p>
<h1 align="center">Dynamic Log</h1>
<p align="center">
  <strong>动态日志级别管理框架 - 运行时刷新日志级别，无需重启应用</strong>
</p>
<p align="center">
    <a target="_blank" href="https://search.maven.org/artifact/com.dynamiclog/dynamic-log-spring">
        <img src="https://img.shields.io/maven-central/v/com.dynamiclog/dynamic-log-spring.svg?label=Maven%20Central" />
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

**核心特性：**
- 🚀 **动态日志级别刷新** - 通过配置中心在运行时更新日志级别，无需重启应用
- 🎯 **默认支持 Apollo/Nacos** - 添加依赖即可自动生效，无需手动实现监听器
- 🔌 **多框架支持** - 可插拔的适配器架构，支持 Logback、Log4j2，可扩展到其他日志系统
- 📊 **事件驱动架构** - 内置事件总线，支持日志级别变更通知和扩展
- 🧩 **插件系统** - 基于 SPI 的插件架构，无需修改核心代码即可自定义扩展
- ⚡ **Spring Boot 自动配置** - 与 Spring Boot 应用零配置集成
- ☕ **JDK 1.8 兼容** - 支持 Java 8 及以上版本

**使用场景：**

- **线上问题排查**：临时调高指定包的日志级别，获取详细日志后快速恢复
- **性能优化**：在生产环境动态降低非关键日志级别，减少日志输出对性能的影响
- **多环境管理**：不同环境使用不同的日志级别策略，通过配置中心统一管理
- **灰度发布**：在灰度期间临时开启详细日志，观察新版本行为
- **安全审计**：动态开启敏感操作的审计日志，满足合规要求

---

## 🏗️ 模块结构

```
Dynamic Log/
├── dynamic-log-dependencies-bom/   # 依赖版本BOM管理模块，统一管理所有依赖版本
├── dynamic-log-common/             # 公共模块 - 注解、异常、常量
├── dynamic-log-core/               # 核心模块 - 核心功能实现，不依赖Spring
├── dynamic-log-spring/             # Spring Boot自动配置 & Apollo/Nacos监听器
├── dynamic-log-test/               # 测试模块 - 集成测试用例
└── dynamic-log-examples/           # 示例应用模块
```

| 模块名 | 说明 |
|--------|------|
| dynamic-log-dependencies-bom | 依赖版本BOM管理，所有依赖版本统一配置 |
| dynamic-log-common | 公共基础模块，注解、异常、常量 |
| dynamic-log-core | 核心实现模块，适配器、管理器、事件总线、插件 |
| dynamic-log-spring | Spring Boot 自动配置、Apollo/Nacos 监听器、Logback 适配器 |
| dynamic-log-test | 测试模块，集成测试用例 |
| dynamic-log-examples | 示例应用模块 |

---

## 📦 安装

### 🍊 Maven

在项目的 `pom.xml` 的 dependencies 中加入以下内容：

```xml
<dependency>
    <groupId>com.dynamiclog</groupId>
    <artifactId>dynamic-log-spring</artifactId>
    <version>1.0.0</version>
</dependency>
```

> 推荐业务项目通过 BOM 方式统一依赖版本：
>
> ```xml
> <dependencyManagement>
>   <dependency>
>     <groupId>com.dynamiclog</groupId>
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
    <groupId>com.dynamiclog</groupId>
    <artifactId>dynamic-log-spring</artifactId>
</dependency>
```

### 2. 配置中心集成

#### Apollo

添加 Apollo 客户端依赖即可自动启用配置监听：

```xml
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

#### Nacos

添加 Nacos 客户端依赖即可自动启用配置监听：

```xml
<dependency>
    <groupId>com.alibaba.nacos</groupId>
    <artifactId>nacos-client</artifactId>
</dependency>
```

在 Nacos 配置中心添加日志级别配置：

```yaml
logging:
  level:
    com.example: DEBUG
    com.example.service: WARN
```

#### Spring Cloud Config

对于 Spring Cloud 配置中心，需要额外添加：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-context</artifactId>
</dependency>
```

并实现 `EnvironmentChangeEvent` 监听器：

```java
@Component
public class LogRefreshListener {
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

### 3. 应用配置

```yaml
dynamic-log:
  enabled: true                    # 启用/禁用框架（默认：true）
  banner-enabled: true             # 启动时显示横幅（默认：true）
  default-adapter: logback         # 默认日志适配器（默认：logback）
  levels:                          # 可选：预定义日志级别
    com.example: DEBUG
    com.example.service: INFO
```

### 4. 编程式使用（不依赖 Spring）

```java
DynamicLogManager manager = DynamicLog.builder()
    .adapter(new LogbackSpringAdapter(loggingSystem))
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

内置适配器：
- `LogbackSpringAdapter` - 适配 Logback 日志系统
- 可扩展支持 Log4j2、JUL 等

### 事件总线（观察者模式）

订阅日志级别变更事件：

```java
eventBus.subscribe(new LogEventListener() {
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

## 🎯 扩展点

### 1. 自定义日志适配器

实现 `LoggingSystemAdapter` 接口以支持其他日志框架：

```java
@Component
public class Log4j2Adapter implements LoggingSystemAdapter {
    @Override
    public String getName() { return "log4j2"; }

    @Override
    public void setLogLevel(String loggerName, String level) {
        // 实现 Log4j2 日志级别设置
    }
    // ... 其他方法
}
```

### 2. 自定义配置监听器

实现配置变更监听，集成其他配置中心：

```java
@Component
public class CustomConfigChangeListener {
    @Autowired
    private DynamicLogManager manager;

    @EventListener
    public void onConfigChange(ConfigChangeEvent event) {
        LogLevelChange change = parseChange(event);
        manager.applyLogLevelChange(change);
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

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| dynamic-log.enabled | boolean | true | 是否启用 Dynamic Log 框架 |
| dynamic-log.banner-enabled | boolean | true | 启动时是否显示横幅 |
| dynamic-log.default-adapter | string | logback | 默认日志适配器名称 |
| dynamic-log.levels | Map | - | 预定义日志级别配置 |

> 以上配置可在 application.yml 或 application.properties 中灵活配置。

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
- [Apollo](https://github.com/ctripframework/apollo) - 携程开源配置中心
- [Nacos](https://nacos.io/) - 阿里巴巴开源配置中心

---

<p align="center">
  <strong>让动态日志管理更简单、更灵活！</strong>
</p>
