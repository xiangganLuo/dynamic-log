package com.dynamiclog.core.event;

/**
 * 用于发布和订阅动态日志事件的事件总线。
 */
public interface LogEventBus {

    /**
     * 同步地向所有已注册的监听器发布事件。
     */
    void publish(LogEvent event);

    /**
     * 异步地发布事件。
     */
    void publishAsync(LogEvent event);

    /**
     * 注册事件监听器。
     */
    void subscribe(LogEventListener listener);

    /**
     * 注销事件监听器。
     */
    void unsubscribe(LogEventListener listener);
}
