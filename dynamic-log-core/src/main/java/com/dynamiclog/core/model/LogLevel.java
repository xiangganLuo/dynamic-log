package com.dynamiclog.core.model;

/**
 * 表示特定logger的日志级别配置。
 */
public final class LogLevel {

    private final String loggerName;
    private final String level;

    public LogLevel(String loggerName, String level) {
        this.loggerName = loggerName;
        this.level = level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "LogLevel{loggerName='" + loggerName + "', level='" + level + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogLevel logLevel = (LogLevel) o;
        return loggerName.equals(logLevel.loggerName) && level.equals(logLevel.level);
    }

    @Override
    public int hashCode() {
        int result = loggerName.hashCode();
        result = 31 * result + level.hashCode();
        return result;
    }
}
