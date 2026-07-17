package com.dynamiclog.log4j2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Log4j2 日志系统适配器自动配置。
 * <p>
 * 仅当 classpath 存在 Log4j2 核心（{@code org.apache.logging.log4j.core.LoggerContext}）、
 * 且 {@code dynamic-log.log4j2.enabled} 未显式关闭时生效。暴露的
 * {@link Log4j2LoggingSystemAdapter} 会被 dynamic-log-spring 的适配器注册收集器
 * 自动收进 registry，使用方通过 {@code dynamic-log.default-adapter: log4j2} 选用。
 */
@Configuration
@ConditionalOnClass(name = "org.apache.logging.log4j.core.LoggerContext")
@ConditionalOnProperty(prefix = "dynamic-log.log4j2", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Log4j2AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Log4j2LoggingSystemAdapter.class)
    public Log4j2LoggingSystemAdapter log4j2LoggingSystemAdapter() {
        return new Log4j2LoggingSystemAdapter();
    }
}
