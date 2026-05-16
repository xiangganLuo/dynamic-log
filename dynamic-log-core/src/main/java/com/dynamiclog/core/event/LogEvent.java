package com.dynamiclog.core.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态日志框架事件的事件对象。
 */
public final class LogEvent {

    private final EventType type;
    private final Map<String, Object> attributes;
    private final long timestamp;

    private LogEvent(Builder builder) {
        this.type = builder.type;
        this.attributes = Collections.unmodifiableMap(new HashMap<>(builder.attributes));
        this.timestamp = builder.timestamp;
    }

    public EventType getType() {
        return type;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private EventType type;
        private final Map<String, Object> attributes = new HashMap<>();
        private long timestamp = System.currentTimeMillis();

        public Builder type(EventType type) {
            this.type = type;
            return this;
        }

        public Builder attribute(String key, Object value) {
            attributes.put(key, value);
            return this;
        }

        public Builder attributes(Map<String, Object> attrs) {
            attributes.putAll(attrs);
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public LogEvent build() {
            if (type == null) {
                throw new IllegalStateException("EventType is required");
            }
            return new LogEvent(this);
        }
    }

    @Override
    public String toString() {
        return "LogEvent{type=" + type + ", attributes=" + attributes + ", timestamp=" + timestamp + '}';
    }
}
