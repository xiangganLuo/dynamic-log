package com.dynamiclog.audit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 审计日志插件配置属性。
 *
 * <pre>
 * dynamic-log:
 *   audit:
 *     enabled: true
 *     logger-name: DYNAMIC-LOG-AUDIT
 *     audit-error: true
 * </pre>
 */
@ConfigurationProperties(prefix = "dynamic-log.audit")
public class DynamicLogAuditProperties {

    /**
     * 是否启用审计日志插件。
     */
    private boolean enabled = true;

    /**
     * 审计正文所用的专用 logger 名称。
     * <p>
     * 使用方可通过日志框架为该 logger 单独配置输出目的地（如独立的审计文件）。
     */
    private String loggerName = "DYNAMIC-LOG-AUDIT";

    /**
     * 是否同时审计级别变更失败（{@code ERROR}）事件。
     */
    private boolean auditError = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public boolean isAuditError() {
        return auditError;
    }

    public void setAuditError(boolean auditError) {
        this.auditError = auditError;
    }
}
