package com.dynamiclog.core.plugin.manage;

import com.dynamiclog.core.plugin.spi.DynamicLogPlugin;
import com.dynamiclog.core.plugin.spi.PluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link PluginManager} 的默认实现。
 * <p>
 * 插件在启动前按其{@code order}属性排序。
 * 销毁时按相反顺序进行。
 */
public class DefaultPluginManager implements PluginManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultPluginManager.class);

    private final Map<String, DynamicLogPlugin> plugins = new ConcurrentHashMap<>();
    private final List<DynamicLogPlugin> orderedPlugins = new ArrayList<>();
    private final PluginContext pluginContext;

    public DefaultPluginManager(PluginContext pluginContext) {
        this.pluginContext = pluginContext;
    }

    @Override
    public void register(DynamicLogPlugin plugin) {
        String id = plugin.getPluginId();
        if (plugins.containsKey(id)) {
            throw new IllegalArgumentException("Plugin already registered: " + id);
        }
        log.info("Initializing plugin: {} v{}", id, plugin.getVersion());
        plugin.init(pluginContext);
        plugins.put(id, plugin);
        orderedPlugins.add(plugin);
        orderedPlugins.sort(Comparator.comparingInt(DynamicLogPlugin::getOrder));
    }

    @Override
    public void unregister(String pluginId) {
        DynamicLogPlugin plugin = plugins.remove(pluginId);
        if (plugin != null) {
            orderedPlugins.remove(plugin);
            try {
                plugin.destroy();
            } catch (Exception e) {
                log.error("Error destroying plugin: {}", pluginId, e);
            }
            log.info("Unregistered plugin: {}", pluginId);
        }
    }

    @Override
    public void startAll() {
        for (DynamicLogPlugin plugin : orderedPlugins) {
            log.info("Starting plugin: {} v{}", plugin.getPluginId(), plugin.getVersion());
            plugin.start();
        }
    }

    @Override
    public void stopAll() {
        List<DynamicLogPlugin> reversed = new ArrayList<>(orderedPlugins);
        Collections.reverse(reversed);
        for (DynamicLogPlugin plugin : reversed) {
            log.info("Stopping plugin: {}", plugin.getPluginId());
            try {
                plugin.stop();
            } catch (Exception e) {
                log.error("Error stopping plugin: {}", plugin.getPluginId(), e);
            }
        }
    }

    @Override
    public DynamicLogPlugin getPlugin(String pluginId) {
        return plugins.get(pluginId);
    }

    @Override
    public Collection<DynamicLogPlugin> getPlugins() {
        return Collections.unmodifiableList(orderedPlugins);
    }

    @Override
    public boolean contains(String pluginId) {
        return plugins.containsKey(pluginId);
    }
}
