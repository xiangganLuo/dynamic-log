package com.dynamiclog.core.refresher;

/**
 * 检测和触发日志级别刷新的策略接口。
 * <p>
 * 不同的实现可以支持各种配置更改源：
 * Spring Cloud EnvironmentChangeEvent、自定义REST端点、定时轮询等。
 */
public interface LogRefresher {

    /**
     * 返回此刷新器策略的名称。
     */
    String getName();

    /**
     * 启动刷新器。在初始化期间调用。
     */
    void start();

    /**
     * 停止刷新器。在关闭期间调用。
     */
    void stop();

    /**
     * 手动触发刷新操作。
     */
    void refresh();

    /**
     * 此刷新器当前是否处于活动状态。
     */
    boolean isRunning();
}
