package com.dynamiclog.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 表示要应用的一组日志级别更改。
 * 创建后不可变。
 */
public final class LogLevelChange {

    private final Map<String, String> levelMap;
    private final long timestamp;

    private LogLevelChange(Builder builder) {
        this.levelMap = Collections.unmodifiableMap(new HashMap<>(builder.levelMap));
        this.timestamp = builder.timestamp;
    }

    public Map<String, String> getLevelMap() {
        return levelMap;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isEmpty() {
        return levelMap.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> levelMap = new HashMap<>();
        private long timestamp = System.currentTimeMillis();

        public Builder putLevel(String loggerName, String level) {
            levelMap.put(loggerName, level);
            return this;
        }

        public Builder putAllLevels(Map<String, String> levels) {
            levelMap.putAll(levels);
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public LogLevelChange build() {
            return new LogLevelChange(this);
        }
    }

    @Override
    public String toString() {
        return "LogLevelChange{levelMap=" + levelMap + ", timestamp=" + timestamp + '}';
    }
}
