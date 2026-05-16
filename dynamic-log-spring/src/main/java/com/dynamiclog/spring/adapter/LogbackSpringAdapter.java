package com.dynamiclog.spring.adapter;

import com.dynamiclog.core.adapter.LoggingSystemAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Spring Boot-based Logback adapter using LoggingSystem abstraction.
 * <p>
 * This adapter leverages Spring Boot's LoggingSystem to manage log levels,
 * providing compatibility with Spring Boot's logging infrastructure.
 */
public class LogbackSpringAdapter implements LoggingSystemAdapter {

    private static final Logger log = LoggerFactory.getLogger(LogbackSpringAdapter.class);
    private static final String NAME = "logback";

    private final LoggingSystem loggingSystem;

    public LogbackSpringAdapter(LoggingSystem loggingSystem) {
        this.loggingSystem = loggingSystem;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setLogLevel(String loggerName, String level) {
        try {
            LogLevel logLevel = LogLevel.valueOf(level.toUpperCase());
            loggingSystem.setLogLevel(loggerName, logLevel);
            log.debug("Set log level: {} -> {}", loggerName, level);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid log level '{}' for logger '{}', skipping", level, loggerName);
        }
    }

    @Override
    public String getLogLevel(String loggerName) {
        LoggerConfiguration config = loggingSystem.getLoggerConfiguration(loggerName);
        if (config != null && config.getEffectiveLevel() != null) {
            return config.getEffectiveLevel().name();
        }
        return null;
    }

    @Override
    public Collection<String> getLoggerNames() {
        return loggingSystem.getLoggerConfigurations()
                .stream()
                .map(LoggerConfiguration::getName)
                .collect(Collectors.toList());
    }

    @Override
    public void resetLogLevel(String loggerName) {
        loggingSystem.setLogLevel(loggerName, null);
        log.debug("Reset log level for: {}", loggerName);
    }
}
