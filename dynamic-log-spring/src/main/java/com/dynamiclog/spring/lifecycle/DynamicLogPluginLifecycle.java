package com.dynamiclog.spring.lifecycle;

import com.dynamiclog.core.plugin.manage.PluginManager;
import com.dynamiclog.core.plugin.spi.DynamicLogPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.util.List;

/**
 * 插件生命周期管理器，实现 Spring SmartLifecycle 接口。
 * <p>
 * 负责在 Spring 应用启动时自动启动所有插件，
 * 在应用关闭时自动停止和销毁插件。
 */
public class DynamicLogPluginLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(DynamicLogPluginLifecycle.class);

    private final PluginManager pluginManager;
    private final List<DynamicLogPlugin> plugins;
    private volatile boolean running = false;

    public DynamicLogPluginLifecycle(PluginManager pluginManager, List<DynamicLogPlugin> plugins) {
        this.pluginManager = pluginManager;
        this.plugins = plugins != null ? plugins : java.util.Collections.emptyList();
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        log.info("启动 Dynamic Log 插件生命周期，注册 {} 个插件", plugins.size());

        // 注册所有插件
        for (DynamicLogPlugin plugin : plugins) {
            try {
                pluginManager.register(plugin);
            } catch (Exception e) {
                log.error("注册插件失败: {}", plugin.getPluginId(), e);
            }
        }

        // 启动所有插件
        pluginManager.startAll();
        running = true;

        log.info("Dynamic Log 插件生命周期启动成功");
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }

        log.info("停止 Dynamic Log 插件");
        log.debug("生命周期停止阶段一：stopAll()");
        pluginManager.stopAll();
        log.debug("生命周期停止阶段二：destroyAll()");
        pluginManager.destroyAll();
        running = false;
        log.debug("Dynamic Log 插件生命周期已停止");
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        // 确保在其他生命周期组件之后停止
        return Integer.MAX_VALUE - 100;
    }
}
