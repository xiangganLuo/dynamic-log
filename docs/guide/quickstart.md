# 快速开始

本节演示在 Spring Boot 项目中接入 Dynamic Log，实现「在配置中心改一行配置，日志级别立即生效」的最短链路。

## 1. 引入依赖

在 `pom.xml` 中加入 Spring Boot 接入模块：

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>dynamic-log-spring</artifactId>
    <version>1.0.0</version>
</dependency>
```

::: tip 推荐用 BOM 统一版本
通过 BOM 统一管理依赖版本，业务侧无需重复写 `<version>`：

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.xiangganluo</groupId>
      <artifactId>dynamic-log-dependencies-bom</artifactId>
      <version>1.0.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```
:::

引入后即触发自动配置，无需任何 `@EnableXxx` 注解——`DynamicLogManager`、Logback 适配器与 `SpringEnvironmentRefresher` 会自动装配。

## 2. 应用配置

在 `application.yml` 中按需开启（均有默认值，可全部省略）：

```yaml
dynamic-log:
  enabled: true                    # 启用框架（默认 true）
  banner-enabled: true             # 启动时打印横幅（默认 true）
  default-adapter: logback         # 默认日志适配器（默认 logback）
  levels:                          # 可选：预置日志级别
    com.example: INFO
    com.example.service: INFO
```

日志级别沿用 Spring Boot 标准的 `logging.level.*` 约定，Dynamic Log 直接从 `Environment` 读取。

## 3. 接入配置中心

按需引入配置中心模块 + 对应客户端，监听器会**自动注册**（无需手写监听逻辑）。以 Apollo 为例：

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>dynamic-log-apollo</artifactId>
    <version>1.0.0</version>
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

Nacos 用法一致：引入 `dynamic-log-nacos` + `spring-cloud-starter-alibaba-nacos-config`。两者机制略有差异，详见 [动态刷新与配置中心](/guide/refresh)。

## 4. 见证运行期刷新

启动应用后，在配置中心把某个包的级别从 `INFO` 改为 `DEBUG` 并发布：

1. 配置中心把变更同步进应用（Apollo 直接回调监听器；Nacos 经 Spring `Environment` 刷新）；
2. 对应模块的监听器检测到 `logging.level.*` 变更，转成 `LogLevelChange`（Nacos 走 `SpringEnvironmentRefresher` 整体重读）；
3. `DynamicLogManager` 通过默认适配器应用新级别；
4. 目标包的 DEBUG 日志立即开始输出——全程无需重启。

排查完成后把配置改回，日志随即恢复。

## 5. 编程式使用（不依赖 Spring）

核心模块可脱离 Spring 单独使用，通过 `DynamicLog.builder()` 构建管理器：

```java
DynamicLogManager manager = DynamicLog.builder()
        .adapter(new LogbackSpringAdapter(loggingSystem)) // 注册日志系统适配器
        .defaultAdapter("logback")                        // 指定默认适配器
        .build();

// 应用一批日志级别变更
LogLevelChange change = LogLevelChange.builder()
        .putLevel("com.example", "DEBUG")
        .putLevel("com.example.service", "INFO")
        .build();
manager.applyLogLevelChange(change);

// 一键复位（ROOT 除外）
manager.resetAllLogLevels();
```

## 下一步

- 理解模型：[核心概念](/guide/concepts)
- 适配其他日志系统：[日志系统适配器](/guide/adapter)
- 订阅变更事件：[事件体系](/guide/events)
- 配置中心细节：[动态刷新与配置中心](/guide/refresh)
