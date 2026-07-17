---
layout: home

hero:
  name: Dynamic Log
  text: 运行期动态刷新日志级别的 Java 框架
  tagline: 从 Apollo / Nacos 等配置中心实时调整日志级别，无需重启应用。核心零 Spring 依赖，Spring Boot 零配置启动。
  image:
    src: /logo.svg
    alt: Dynamic Log
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/quickstart
    - theme: alt
      text: 什么是 Dynamic Log
      link: /guide/introduction
    - theme: alt
      text: GitHub
      link: https://github.com/xiangganLuo/dynamic-log

features:
  - icon: 🚀
    title: 运行期动态刷新
    details: 通过配置中心在运行时更新日志级别，秒级生效、无需重启应用，让线上问题排查快人一步。
    link: /guide/refresh
    linkText: 动态刷新
  - icon: 🎯
    title: Apollo / Nacos 开箱即用
    details: Apollo、Nacos 各为独立模块，按需引入即自动注册监听器；在配置中心改 logging.level.* 实时生效，无需手写监听逻辑。
    link: /guide/refresh
    linkText: 配置中心接入
  - icon: 🔌
    title: 多日志框架适配
    details: 以 LoggingSystemAdapter 策略接口抽象日志系统，已提供 Logback 与 Log4j2 官方适配器，可平滑扩展到其他日志系统。
    link: /guide/adapter
    linkText: 适配器
  - icon: ⏳
    title: 临时调级 · 自动回滚
    details: TTL 插件临时把某个包调到 DEBUG，到期自动恢复原级别，命中「排查完自动善后」，不怕忘记回滚导致日志爆量。
    link: /guide/plugins-official
    linkText: 官方插件
  - icon: 🛠️
    title: REST 端点 + 审计
    details: 端点模块提供查询/设置日志级别的 HTTP 接口（支持临时级别）；审计插件把每次变更写入专用审计 logger，便于合规追溯。
    link: /guide/plugins-official
    linkText: 官方插件
  - icon: ⚡
    title: Spring Boot 真开箱即用
    details: 引入 dynamic-log-spring 即自动注册 Logback 适配器、装配管理器与刷新器，additive 应用 dynamic-log.levels，容器中的插件 Bean 自动收集。
    link: /guide/springboot
    linkText: Spring Boot 接入
---

## 一分钟接入

在 `pom.xml` 引入 Spring Boot 接入模块：

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>dynamic-log-spring</artifactId>
    <version>1.0.0</version>
</dependency>
```

按需引入配置中心模块（以 Apollo 为例）+ 对应客户端，监听器随之自动注册：

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

在配置中心用标准的 `logging.level.*` 键调整日志级别，无需重启即刻生效：

```properties
logging.level.com.example=DEBUG
logging.level.com.example.service=INFO
```

更多细节见 [快速开始](/guide/quickstart) 与 [动态刷新与配置中心](/guide/refresh)。
