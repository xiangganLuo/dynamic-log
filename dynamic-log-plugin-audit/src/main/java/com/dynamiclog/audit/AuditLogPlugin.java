package com.dynamiclog.audit;

import com.dynamiclog.core.event.EventType;
import com.dynamiclog.core.event.LogEvent;
import com.dynamiclog.core.event.LogEventBus;
import com.dynamiclog.core.event.LogEventListener;
import com.dynamiclog.core.model.LogLevelChange;
import com.dynamiclog.core.plugin.spi.DynamicLogPlugin;
import com.dynamiclog.core.plugin.spi.PluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审计日志插件。
 * <p>
 * 订阅事件总线上的日志级别变更事件，将「谁 / 何时 / 改了哪个 logger → 什么级别 /
 * 用哪个适配器」写入专用审计 logger（默认 {@code DYNAMIC-LOG-AUDIT}），便于使用方
 * 为审计正文单独配置输出目的地（如独立的审计文件）并做合规追溯。
 * <p>
 * 默认审计 {@link EventType#LOG_LEVEL_CHANGED}（级别变更已生效）；当
 * {@code dynamic-log.audit.audit-error=true}（默认）时，同时审计
 * {@link EventType#ERROR}（级别变更失败）。
 */
public class AuditLogPlugin implements DynamicLogPlugin {

    private static final Logger log = LoggerFactory.getLogger(AuditLogPlugin.class);

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    private final DynamicLogAuditProperties properties;

    /** 审计正文专用 logger，按配置的名称解析。 */
    private final Logger auditLog;

    private PluginContext context;
    private LogEventBus eventBus;
    private LogEventListener listener;

    public AuditLogPlugin(DynamicLogAuditProperties properties) {
        this.properties = properties;
        this.auditLog = LoggerFactory.getLogger(properties.getLoggerName());
    }

    @Override
    public String getPluginId() {
        return "audit-log-plugin";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public void init(PluginContext context) {
        this.context = context;
        log.debug("审计日志插件初始化，审计 logger=[{}], auditError={}",
                properties.getLoggerName(), properties.isAuditError());
    }

    @Override
    public void start() {
        this.eventBus = context.getEventBus();
        this.listener = new AuditEventListener();
        eventBus.subscribe(listener);
        log.debug("审计日志插件启动，已订阅事件总线，开始监听日志级别变更");
    }

    @Override
    public void stop() {
        if (eventBus != null && listener != null) {
            eventBus.unsubscribe(listener);
            log.debug("审计日志插件停止，已从事件总线反注册监听器");
        }
        this.listener = null;
    }

    @Override
    public void destroy() {
        this.eventBus = null;
        this.context = null;
        log.debug("审计日志插件销毁，资源已清理");
    }

    /**
     * 处理级别变更已生效事件，输出审计正文。
     */
    private void handleLevelChanged(LogEvent event) {
        LogLevelChange change = event.getAttribute("change");
        String adapter = event.getAttribute("adapter");
        auditLog.info("[审计] 日志级别变更已生效 | 时间={} | 适配器={} | 变更明细={}",
                formatTimestamp(event.getTimestamp()), adapter, formatChange(change));
    }

    /**
     * 处理级别变更失败事件，输出审计正文。
     */
    private void handleError(LogEvent event) {
        LogLevelChange change = event.getAttribute("change");
        String adapter = event.getAttribute("adapter");
        Throwable error = event.getAttribute("error");
        auditLog.info("[审计] 日志级别变更失败 | 时间={} | 适配器={} | 变更明细={} | 原因={}",
                formatTimestamp(event.getTimestamp()), adapter, formatChange(change),
                error != null ? error.getMessage() : "未知");
    }

    /**
     * 将变更渲染为「logger → 级别」明细。
     */
    private String formatChange(LogLevelChange change) {
        if (change == null) {
            return "(空)";
        }
        Map<String, String> levelMap = change.getLevelMap();
        if (levelMap.isEmpty()) {
            return "(空)";
        }
        return levelMap.entrySet().stream()
                .map(e -> e.getKey() + " → " + e.getValue())
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String formatTimestamp(long timestamp) {
        return TIMESTAMP_FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }

    /**
     * 事件监听器：按事件类型分发到审计处理。
     */
    private final class AuditEventListener implements LogEventListener {

        @Override
        public void onEvent(LogEvent event) {
            EventType type = event.getType();
            if (type == EventType.LOG_LEVEL_CHANGED) {
                log.debug("审计插件收到事件: {}", type);
                handleLevelChanged(event);
            } else if (type == EventType.ERROR && properties.isAuditError()) {
                log.debug("审计插件收到事件: {}", type);
                handleError(event);
            }
        }

        @Override
        public int getOrder() {
            return 10;
        }
    }
}
