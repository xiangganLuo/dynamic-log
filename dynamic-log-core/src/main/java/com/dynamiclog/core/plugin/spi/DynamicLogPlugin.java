package com.dynamiclog.core.plugin.spi;

/**
 * 动态日志框架的插件接口。
 * <p>
 * 允许通过自定义适配器、刷新器、事件监听器和其他功能扩展框架，
 * 而无需修改核心代码。
 */
public interface DynamicLogPlugin {

    /**
     * 返回此插件的唯一标识符。
     */
    String getPluginId();

    /**
     * 返回此插件的版本。
     */
    default String getVersion() {
        return "1.0.0";
    }

    /**
     * 返回执行顺序。较低的值先执行。
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 在插件初始化时调用。
     */
    void init(PluginContext context);

    /**
     * 在插件启动时调用。
     */
    void start();

    /**
     * 在插件停止时调用。
     */
    void stop();

    /**
     * 在插件销毁时调用。
     */
    void destroy();
}
