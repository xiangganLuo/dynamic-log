package com.dynamiclog.log4j2;

import com.dynamiclog.core.adapter.LoggingSystemAdapter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 基于 Log4j2 原生 API 的日志系统适配器。
 * <p>
 * 通过 {@link Configurator} 动态设置/重置日志级别，
 * 通过当前 {@link LoggerContext} 的 {@code Configuration} 收集已配置的 logger 名称，
 * 使框架在使用 Log4j2 作为日志实现时同样可以运行时调整日志级别。
 * <p>
 * 使用方通过 {@code dynamic-log.default-adapter: log4j2} 选用本适配器。
 */
public class Log4j2LoggingSystemAdapter implements LoggingSystemAdapter {

    private static final Logger log = LoggerFactory.getLogger(Log4j2LoggingSystemAdapter.class);
    private static final String NAME = "log4j2";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setLogLevel(String loggerName, String level) {
        // level 为 null 表示重置该 logger，交由 Log4j2 从父级继承
        if (level == null) {
            resetLogLevel(loggerName);
            return;
        }
        try {
            Level log4j2Level = Level.valueOf(level.toUpperCase());
            Configurator.setLevel(loggerName, log4j2Level);
            log.debug("Set log level: {} -> {}", loggerName, level);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid log level '{}' for logger '{}', skipping", level, loggerName);
        }
    }

    @Override
    public String getLogLevel(String loggerName) {
        Level level = LogManager.getLogger(loggerName).getLevel();
        return level != null ? level.name() : null;
    }

    @Override
    public Collection<String> getLoggerNames() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Collection<String> names = new ArrayList<>(context.getConfiguration().getLoggers().keySet());
        if (log.isDebugEnabled()) {
            log.debug("Collected logger names from Log4j2 configuration: {}", names);
        }
        return names;
    }

    @Override
    public void resetLogLevel(String loggerName) {
        // 将级别设为 null，使该 logger 恢复从父级继承
        Configurator.setLevel(loggerName, (Level) null);
        log.debug("Reset log level for: {}", loggerName);
    }
}
