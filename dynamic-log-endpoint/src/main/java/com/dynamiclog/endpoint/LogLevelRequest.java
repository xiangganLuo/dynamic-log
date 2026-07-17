package com.dynamiclog.endpoint;

/**
 * 设置日志级别的请求体。
 *
 * <pre>
 * {
 *   "logger": "com.example.demo",
 *   "level": "DEBUG",
 *   "ttlSeconds": 300
 * }
 * </pre>
 * <p>
 * {@code ttlSeconds} 可选：为空或非正数表示永久设置；为正数表示临时设置，
 * 到期后自动回滚到设置前的级别。
 */
public class LogLevelRequest {

    /**
     * 目标 logger 名称，例如 {@code com.example.demo} 或 {@code ROOT}。
     */
    private String logger;

    /**
     * 目标日志级别，例如 {@code DEBUG}、{@code INFO}。大小写不敏感。
     */
    private String level;

    /**
     * 临时级别有效期（秒）。为空或非正数表示永久设置。
     */
    private Integer ttlSeconds;

    public String getLogger() {
        return logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Integer getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(Integer ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public String toString() {
        return "LogLevelRequest{logger='" + logger + "', level='" + level + "', ttlSeconds=" + ttlSeconds + '}';
    }
}
