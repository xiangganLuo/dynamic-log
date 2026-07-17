package com.dynamiclog.ttl;

import com.dynamiclog.core.adapter.LoggingSystemAdapter;
import com.dynamiclog.core.adapter.LoggingSystemAdapterRegistry;
import com.dynamiclog.core.manager.DynamicLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 临时日志级别管理器：临时调整某个 logger 的日志级别，并在 TTL 到期后自动回滚。
 * <p>
 * 面向「线上排查完快速恢复」的痛点：临时把某个包/类的级别调到 {@code DEBUG} 排查问题，
 * 到点自动恢复为原级别（或恢复为继承父级），无需人工善后，避免忘记回滚导致日志爆量。
 * <p>
 * 语义要点：
 * <ul>
 *   <li>应用前用默认（或指定）适配器 {@link LoggingSystemAdapter#getLogLevel(String)} 记下原级别，
 *       {@code null} 表示该 logger 未显式设置（继承父级），回滚时用
 *       {@link LoggingSystemAdapter#resetLogLevel(String)}。</li>
 *   <li>对同一 logger 重复 {@code applyTemporary} 时，取消旧的待回滚任务（避免旧任务把新设置回滚），
 *       并保留 <b>最初一次</b> 记录的原始级别（不会把「当前临时值」误当作原值）。</li>
 *   <li>{@link #cancel(String)} 立即回滚并清除任务；{@link #shutdown()} 关闭调度器。</li>
 * </ul>
 * 线程安全：所有状态变更在单一锁下进行，回滚调度使用单线程 daemon 调度器。
 */
public class TemporaryLogLevelManager {

    private static final Logger log = LoggerFactory.getLogger(TemporaryLogLevelManager.class);

    private final DynamicLogManager manager;
    private final ScheduledExecutorService scheduler;

    /** 每个 logger 的待回滚任务，用于重复应用/取消时精确取消。 */
    private final ConcurrentHashMap<String, ScheduledFuture<?>> pendingRollbacks = new ConcurrentHashMap<>();

    /** 每个 logger 最初一次记录的原始级别（含解析出的适配器名），到期或取消时据此回滚。 */
    private final ConcurrentHashMap<String, OriginalLevel> originalLevels = new ConcurrentHashMap<>();

    /** 保护 pendingRollbacks / originalLevels 的一致性，并与回滚回调互斥。 */
    private final Object lock = new Object();

    private volatile boolean shutdown = false;

    public TemporaryLogLevelManager(DynamicLogManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("DynamicLogManager 不能为空");
        }
        this.manager = manager;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "dynamic-log-ttl-rollback");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 使用默认适配器临时调整日志级别，到期后自动回滚。
     *
     * @param loggerName     logger 名称
     * @param level          临时日志级别（例如 {@code DEBUG}）
     * @param durationMillis TTL 毫秒数，必须为正
     */
    public void applyTemporary(String loggerName, String level, long durationMillis) {
        applyTemporary(loggerName, level, durationMillis, null);
    }

    /**
     * 使用指定适配器临时调整日志级别，到期后自动回滚。
     *
     * @param loggerName     logger 名称
     * @param level          临时日志级别（例如 {@code DEBUG}）
     * @param durationMillis TTL 毫秒数，必须为正
     * @param adapterName    适配器名称，{@code null} 表示使用默认适配器
     */
    public void applyTemporary(String loggerName, String level, long durationMillis, String adapterName) {
        if (loggerName == null || loggerName.trim().isEmpty()) {
            throw new IllegalArgumentException("loggerName 不能为空");
        }
        if (level == null || level.trim().isEmpty()) {
            throw new IllegalArgumentException("level 不能为空");
        }
        if (durationMillis <= 0) {
            throw new IllegalArgumentException("durationMillis 必须为正: " + durationMillis);
        }

        synchronized (lock) {
            if (shutdown) {
                throw new IllegalStateException("TemporaryLogLevelManager 已关闭");
            }

            LoggingSystemAdapter adapter = resolveAdapter(adapterName);
            String usedAdapter = adapter.getName();

            // 取消旧的待回滚任务，避免旧任务把即将设置的新级别回滚
            ScheduledFuture<?> old = pendingRollbacks.remove(loggerName);
            if (old != null) {
                old.cancel(false);
                log.debug("取消 logger [{}] 旧的待回滚任务", loggerName);
            }

            // 保留最初一次记录的原始级别：已处于临时态时不覆盖（避免用当前临时值当原值）
            OriginalLevel original = originalLevels.get(loggerName);
            if (original == null) {
                String current = adapter.getLogLevel(loggerName);
                original = new OriginalLevel(current, usedAdapter);
                originalLevels.put(loggerName, original);
            }

            log.debug("临时调整日志级别: logger=[{}] 原级别=[{}] 新级别=[{}] TTL={}ms 适配器=[{}]",
                    loggerName, original.level, level, durationMillis, usedAdapter);

            adapter.setLogLevel(loggerName, level);
            log.info("已临时将 logger [{}] 级别调整为 [{}]，{}ms 后自动回滚", loggerName, level, durationMillis);

            AtomicReference<ScheduledFuture<?>> selfRef = new AtomicReference<>();
            ScheduledFuture<?> future = scheduler.schedule(
                    () -> rollbackOnExpiry(loggerName, selfRef.get()), durationMillis, TimeUnit.MILLISECONDS);
            selfRef.set(future);
            pendingRollbacks.put(loggerName, future);
        }
    }

    /**
     * 立即回滚指定 logger 的临时级别并清除其待回滚任务。若该 logger 当前无临时态则忽略。
     */
    public void cancel(String loggerName) {
        if (loggerName == null) {
            return;
        }
        synchronized (lock) {
            ScheduledFuture<?> future = pendingRollbacks.remove(loggerName);
            if (future != null) {
                future.cancel(false);
            }
            if (originalLevels.containsKey(loggerName)) {
                doRollback(loggerName);
                originalLevels.remove(loggerName);
                log.debug("cancel: 已立即回滚 logger [{}]", loggerName);
            }
        }
    }

    /**
     * 关闭调度器。容器关闭时由 {@code @Bean(destroyMethod="shutdown")} 调用。
     * <p>
     * 关闭时不再执行到期回滚（应用即将退出，日志系统随之销毁），仅停止调度线程并清理状态。
     */
    public void shutdown() {
        synchronized (lock) {
            if (shutdown) {
                return;
            }
            shutdown = true;
            log.debug("关闭 TemporaryLogLevelManager 调度器，丢弃 {} 个待回滚任务", pendingRollbacks.size());
            scheduler.shutdownNow();
            pendingRollbacks.clear();
            originalLevels.clear();
        }
    }

    /**
     * TTL 到期时的回滚回调。仅当自身仍是该 logger 的当前待回滚任务时才执行，
     * 否则说明已被后续 {@code applyTemporary}/{@code cancel} 取代，直接跳过。
     */
    private void rollbackOnExpiry(String loggerName, ScheduledFuture<?> self) {
        synchronized (lock) {
            if (shutdown) {
                return;
            }
            if (pendingRollbacks.get(loggerName) != self) {
                // 已被替换或取消，放弃本次回滚，避免把新设置回滚掉
                return;
            }
            doRollback(loggerName);
            pendingRollbacks.remove(loggerName);
            originalLevels.remove(loggerName);
        }
    }

    /**
     * 依据最初记录的原始级别执行回滚。原级别非空→重新设置；原级别为空→重置为继承父级。
     * 调用方持有 {@link #lock}。
     */
    private void doRollback(String loggerName) {
        OriginalLevel original = originalLevels.get(loggerName);
        if (original == null) {
            return;
        }
        LoggingSystemAdapter adapter = resolveAdapter(original.adapterName);
        if (original.level != null) {
            adapter.setLogLevel(loggerName, original.level);
            log.debug("回滚 logger [{}] → 原级别 [{}]", loggerName, original.level);
        } else {
            adapter.resetLogLevel(loggerName);
            log.debug("回滚 logger [{}] → 继承父级（重置）", loggerName);
        }
    }

    private LoggingSystemAdapter resolveAdapter(String adapterName) {
        LoggingSystemAdapterRegistry registry = manager.getAdapterRegistry();
        LoggingSystemAdapter adapter = adapterName != null
                ? registry.getAdapter(adapterName)
                : registry.getDefaultAdapter();
        if (adapter == null) {
            throw new IllegalStateException("未找到日志系统适配器: "
                    + (adapterName != null ? adapterName : "<default>"));
        }
        return adapter;
    }

    /**
     * 记录某个 logger 进入临时态之前的原始级别。
     * {@code level} 为 {@code null} 表示原本未显式设置（继承父级）。
     */
    private static final class OriginalLevel {
        private final String level;
        private final String adapterName;

        OriginalLevel(String level, String adapterName) {
            this.level = level;
            this.adapterName = adapterName;
        }
    }
}
