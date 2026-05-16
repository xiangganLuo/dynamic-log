package com.dynamiclog.core.event;

/**
 * 动态日志事件的监听器。
 */
public interface LogEventListener {

    /**
     * 当事件发布时调用。
     *
     * @param event 事件
     */
    void onEvent(LogEvent event);

    /**
     * 返回此监听器的优先级。较低的值先执行。
     */
    default int getOrder() {
        return 0;
    }
}
