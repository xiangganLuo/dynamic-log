package com.dynamiclog.core.adapter;

import java.util.Collection;
import java.util.Map;

/**
 * 不同日志系统实现的策略接口。
 * <p>
 * 实现类将动态日志框架适配到特定的日志系统（Logback、Log4j2等），
 * 为日志级别管理提供统一的API。
 */
public interface LoggingSystemAdapter {

    /**
     * 返回此适配器的名称（例如，"logback"、"log4j2"）。
     */
    String getName();

    /**
     * 设置特定logger的日志级别。
     *
     * @param loggerName logger名称
     * @param level      日志级别字符串（例如，"DEBUG"、"INFO"），或null以重置
     */
    void setLogLevel(String loggerName, String level);

    /**
     * 返回特定logger的当前日志级别。
     *
     * @param loggerName logger名称
     * @return 当前级别字符串，如果未显式设置则返回null
     */
    String getLogLevel(String loggerName);

    /**
     * 返回所有当前配置的logger名称。
     */
    Collection<String> getLoggerNames();

    /**
     * 重置logger以从其父级继承级别。
     *
     * @param loggerName 要重置的logger名称
     */
    void resetLogLevel(String loggerName);

    /**
     * 原子性地应用一批日志级别更改。
     * 默认实现遍历并为每个条目调用setLogLevel。
     *
     * @param levelMap logger名称到级别的映射
     */
    default void applyLevels(Map<String, String> levelMap) {
        levelMap.forEach(this::setLogLevel);
    }

    /**
     * 重置不在提供集合中的所有logger（排除ROOT）。
     *
     * @param excludeLoggerNames 保持不变的logger名称
     */
    default void resetAbsentLoggers(Collection<String> excludeLoggerNames) {
        getLoggerNames().stream()
                .filter(name -> !excludeLoggerNames.contains(name))
                .filter(name -> !"ROOT".equalsIgnoreCase(name))
                .forEach(this::resetLogLevel);
    }
}
