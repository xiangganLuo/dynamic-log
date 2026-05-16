package com.dynamiclog.example.plugin;

import com.dynamiclog.core.event.EventType;
import com.dynamiclog.core.event.LogEvent;
import com.dynamiclog.core.event.LogEventListener;
import com.dynamiclog.core.plugin.spi.DynamicLogPlugin;
import com.dynamiclog.core.plugin.spi.PluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 审计日志插件示例。
 * <p>
 * 监听所有日志级别变更事件，并记录审计信息。
 * 演示如何通过插件机制扩展框架功能。
 */
@Component
public class AuditLogPlugin implements DynamicLogPlugin {

    private static final Logger log = LoggerFactory.getLogger(AuditLogPlugin.class);
    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    private PluginContext context;

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
        log.info("审计日志插件初始化");
    }

    @Override
    public void start() {
        // 注册事件监听器
        context.getEventBus().subscribe(new LogEventListener() {
            @Override
            public void onEvent(LogEvent event) {
                if (event.getType() == EventType.LOG_LEVEL_CHANGED) {
                    handleLogLevelChanged(event);
                }
            }

            @Override
            public int getOrder() {
                return 10;
            }
        });
        log.info("审计日志插件启动，开始监听日志级别变更");
    }

    @Override
    public void stop() {
        log.info("审计日志插件停止");
    }

    @Override
    public void destroy() {
        log.info("审计日志插件销毁");
    }

    private void handleLogLevelChanged(LogEvent event) {
        Object changeObj = event.getAttribute("change");
        String adapter = event.getAttribute("adapter");
        auditLog.info("日志级别变更 - 适配器: {}, 变更: {}, 时间: {}",
                adapter, changeObj, event.getTimestamp());
    }
}
