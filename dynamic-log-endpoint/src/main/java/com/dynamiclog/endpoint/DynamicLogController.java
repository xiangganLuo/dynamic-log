package com.dynamiclog.endpoint;

import com.dynamiclog.core.adapter.LoggingSystemAdapter;
import com.dynamiclog.core.manager.DynamicLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 运行时日志级别管理的 REST 端点。
 * <p>
 * 提供两个接口：
 * <ul>
 *   <li>{@code GET /dynamic-log/levels}：列出默认适配器中所有 logger 的当前级别。</li>
 *   <li>{@code POST /dynamic-log/level}：设置指定 logger 的级别，支持通过
 *       {@code ttlSeconds} 指定临时级别，到期后自动回滚。</li>
 * </ul>
 * <p>
 * 临时级别的回滚由本类自包含的单线程守护调度器实现：设置前记录原级别，到期后恢复。
 * 对同一 logger 重复临时设置时，取消旧的回滚任务并保留最初的原级别，避免链式回滚
 * 只能退回到上一次临时值。
 */
@RestController
@RequestMapping("/dynamic-log")
public class DynamicLogController implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(DynamicLogController.class);

    /**
     * 合法的日志级别集合（对齐 Spring Boot {@code LogLevel}）。
     */
    private static final Set<String> VALID_LEVELS = new java.util.HashSet<>(
            java.util.Arrays.asList("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF"));

    private final DynamicLogManager manager;

    /**
     * 单线程守护调度器，专用于临时级别到期回滚。
     */
    private final ScheduledExecutorService rollbackScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "dynamic-log-endpoint-rollback");
        t.setDaemon(true);
        return t;
    });

    /**
     * 记录每个 logger 当前挂起的临时回滚任务，键为 logger 名称。
     */
    private final Map<String, TempOverride> overrides = new ConcurrentHashMap<>();

    public DynamicLogController(DynamicLogManager manager) {
        this.manager = manager;
    }

    /**
     * 列出默认适配器中所有 logger 的当前级别。
     */
    @GetMapping("/levels")
    public ResponseEntity<Object> levels() {
        LoggingSystemAdapter adapter = manager.getAdapterRegistry().getDefaultAdapter();
        if (adapter == null) {
            log.debug("查询日志级别失败：无可用的默认适配器");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(error("无可用的默认日志适配器"));
        }

        Collection<String> loggerNames = adapter.getLoggerNames();
        Map<String, String> result = new LinkedHashMap<>();
        for (String name : loggerNames) {
            result.put(name, adapter.getLogLevel(name));
        }
        log.debug("查询日志级别：适配器=[{}], logger 数量={}", adapter.getName(), result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 设置指定 logger 的日志级别。
     * <p>
     * 无 {@code ttlSeconds}（或非正数）时永久设置；有正数 {@code ttlSeconds} 时临时设置，
     * 到期后自动回滚到设置前的级别。
     */
    @PostMapping("/level")
    public ResponseEntity<Object> setLevel(@RequestBody(required = false) LogLevelRequest request) {
        if (request == null) {
            log.debug("设置日志级别失败：请求体为空");
            return badRequest("请求体不能为空");
        }

        String logger = request.getLogger();
        if (logger == null || logger.trim().isEmpty()) {
            log.debug("设置日志级别失败：logger 为空, request={}", request);
            return badRequest("logger 不能为空");
        }
        logger = logger.trim();

        String level = request.getLevel();
        if (level == null || level.trim().isEmpty()) {
            log.debug("设置日志级别失败：level 为空, logger={}", logger);
            return badRequest("level 不能为空");
        }
        level = level.trim().toUpperCase();
        if (!VALID_LEVELS.contains(level)) {
            log.debug("设置日志级别失败：非法 level=[{}], logger={}", request.getLevel(), logger);
            return badRequest("非法的日志级别: " + request.getLevel() + "，合法值: " + VALID_LEVELS);
        }

        LoggingSystemAdapter adapter = manager.getAdapterRegistry().getDefaultAdapter();
        if (adapter == null) {
            log.debug("设置日志级别失败：无可用的默认适配器, logger={}", logger);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(error("无可用的默认日志适配器"));
        }

        Integer ttlSeconds = request.getTtlSeconds();
        if (ttlSeconds != null && ttlSeconds > 0) {
            applyTemporary(adapter, logger, level, ttlSeconds);
            log.info("临时设置日志级别: logger={}, level={}, ttl={}s", logger, level, ttlSeconds);
            Map<String, Object> body = ok(logger, level);
            body.put("ttlSeconds", ttlSeconds);
            return ResponseEntity.ok(body);
        }

        applyPermanent(adapter, logger, level);
        log.info("永久设置日志级别: logger={}, level={}", logger, level);
        return ResponseEntity.ok(ok(logger, level));
    }

    /**
     * 永久设置：若该 logger 存在挂起的临时回滚任务，先取消它，避免随后被回滚覆盖。
     */
    private void applyPermanent(LoggingSystemAdapter adapter, String logger, String level) {
        synchronized (overrides) {
            TempOverride existing = overrides.remove(logger);
            if (existing != null) {
                existing.future.cancel(false);
                log.debug("永久设置覆盖了挂起的临时回滚: logger={}", logger);
            }
        }
        log.debug("应用永久日志级别: logger={}, level={}", logger, level);
        adapter.setLogLevel(logger, level);
    }

    /**
     * 临时设置：记录原级别，设置新级别并安排到期回滚。
     * <p>
     * 对同一 logger 重复设置时取消旧回滚任务，但保留最初记录的原级别，
     * 确保回滚始终退回到首次临时设置前的状态。
     */
    private void applyTemporary(LoggingSystemAdapter adapter, String logger, String level, int ttlSeconds) {
        synchronized (overrides) {
            TempOverride existing = overrides.get(logger);
            String originalLevel;
            if (existing != null) {
                existing.future.cancel(false);
                originalLevel = existing.originalLevel;
                log.debug("临时设置取消旧回滚，保留最初原级别: logger={}, 原级别={}", logger, originalLevel);
            } else {
                originalLevel = adapter.getLogLevel(logger);
                log.debug("临时设置记录原级别: logger={}, 原级别={}", logger, originalLevel);
            }

            adapter.setLogLevel(logger, level);

            TempOverride override = new TempOverride(originalLevel);
            override.future = rollbackScheduler.schedule(
                    () -> rollback(adapter, logger, override), ttlSeconds, TimeUnit.SECONDS);
            overrides.put(logger, override);
        }
    }

    /**
     * 到期回滚：仅当映射仍指向本任务时才恢复，避免与后续设置竞争。
     */
    private void rollback(LoggingSystemAdapter adapter, String logger, TempOverride expected) {
        synchronized (overrides) {
            if (!overrides.remove(logger, expected)) {
                // 已被新的设置替换或取消，无需回滚
                return;
            }
        }
        try {
            String originalLevel = expected.originalLevel;
            if (originalLevel == null) {
                adapter.resetLogLevel(logger);
            } else {
                adapter.setLogLevel(logger, originalLevel);
            }
            log.debug("临时日志级别到期回滚: logger={}, 恢复级别={}", logger, originalLevel);
        } catch (Exception e) {
            log.warn("临时日志级别回滚失败: logger={}", logger, e);
        }
    }

    @Override
    public void destroy() {
        rollbackScheduler.shutdownNow();
        log.debug("dynamic-log-endpoint 回滚调度器已关闭");
    }

    private static ResponseEntity<Object> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(message));
    }

    private static Map<String, Object> error(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        return body;
    }

    private static Map<String, Object> ok(String logger, String level) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("logger", logger);
        body.put("level", level);
        return body;
    }

    /**
     * 挂起的临时级别覆盖：记录首次设置前的原级别及其回滚任务句柄。
     */
    private static final class TempOverride {
        private final String originalLevel;
        private volatile ScheduledFuture<?> future;

        private TempOverride(String originalLevel) {
            this.originalLevel = originalLevel;
        }
    }
}
