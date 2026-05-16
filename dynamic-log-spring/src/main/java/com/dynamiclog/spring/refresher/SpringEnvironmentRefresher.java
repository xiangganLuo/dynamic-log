package com.dynamiclog.spring.refresher;

import com.dynamiclog.common.constants.DynamicLogConstants;
import com.dynamiclog.core.manager.DynamicLogManager;
import com.dynamiclog.core.model.LogLevelChange;
import com.dynamiclog.core.refresher.LogRefresher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Refresher that reads log level configuration directly from Spring Environment.
 * <p>
 * This is the primary refresher used in non-Spring-Cloud scenarios.
 * It reads all "logging.level.*" properties from the environment and applies them.
 */
public class SpringEnvironmentRefresher implements LogRefresher {

    private static final Logger log = LoggerFactory.getLogger(SpringEnvironmentRefresher.class);

    private final DynamicLogManager logManager;
    private final ConfigurableApplicationContext applicationContext;
    private final LoggingSystem loggingSystem;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public SpringEnvironmentRefresher(DynamicLogManager logManager,
                                       ConfigurableApplicationContext applicationContext,
                                       LoggingSystem loggingSystem) {
        this.logManager = logManager;
        this.applicationContext = applicationContext;
        this.loggingSystem = loggingSystem;
    }

    @Override
    public String getName() {
        return "spring-environment";
    }

    @Override
    public void start() {
        running.set(true);
        log.info("Spring Environment refresher started");
    }

    @Override
    public void stop() {
        running.set(false);
        log.info("Spring Environment refresher stopped");
    }

    @Override
    public void refresh() {
        if (!running.get()) {
            return;
        }
        log.info("Refreshing log levels from Spring Environment");
        applyFromEnvironment();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private void applyFromEnvironment() {
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        Map<String, String> levelMap = new HashMap<>();

        for (PropertySource<?> ps : env.getPropertySources()) {
            if (ps instanceof EnumerablePropertySource) {
                EnumerablePropertySource<?> eps = (EnumerablePropertySource<?>) ps;
                for (String name : eps.getPropertyNames()) {
                    if (name.startsWith(DynamicLogConstants.LOGGING_PROPERTY_PREFIX + ".")) {
                        String loggerName = name.substring(DynamicLogConstants.LOGGING_PROPERTY_PREFIX.length() + 1);
                        String level = String.valueOf(eps.getProperty(name));
                        levelMap.put(loggerName, level.toUpperCase());
                    }
                }
            }
        }

        if (!levelMap.isEmpty()) {
            LogLevelChange change = LogLevelChange.builder()
                    .putAllLevels(levelMap)
                    .build();
            logManager.applyLogLevelChange(change);
        } else {
            log.debug("No logging.level.* properties found in environment");
        }
    }
}
