package com.dynamiclog.endpoint;

import com.dynamiclog.core.adapter.DefaultLoggingSystemAdapterRegistry;
import com.dynamiclog.core.adapter.LoggingSystemAdapter;
import com.dynamiclog.core.event.DefaultLogEventBus;
import com.dynamiclog.core.manager.DynamicLogManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证 {@link DynamicLogController} 的查询、永久设置与 TTL 临时设置到期回滚行为。
 * <p>
 * 使用内存假适配器 + 真实注册表 + 真实 {@link DynamicLogManager} 直接构造 controller，
 * 不依赖 web 容器。
 */
class DynamicLogControllerTest {

    private InMemoryAdapter adapter;
    private DynamicLogController controller;

    @BeforeEach
    void setUp() {
        adapter = new InMemoryAdapter();
        DefaultLoggingSystemAdapterRegistry registry = new DefaultLoggingSystemAdapterRegistry();
        registry.register(adapter);
        DynamicLogManager manager = new DynamicLogManager(registry, new DefaultLogEventBus());
        controller = new DynamicLogController(manager);
    }

    @AfterEach
    void tearDown() {
        controller.destroy();
    }

    @Test
    @SuppressWarnings("unchecked")
    void levelsReturnsCurrentLevelMap() {
        adapter.setLogLevel("com.foo", "INFO");
        adapter.setLogLevel("com.bar", "WARN");

        ResponseEntity<Object> response = controller.levels();

        assertEquals(200, response.getStatusCodeValue());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("INFO", body.get("com.foo"));
        assertEquals("WARN", body.get("com.bar"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void permanentSetTakesEffect() {
        LogLevelRequest request = new LogLevelRequest();
        request.setLogger("com.foo.perm");
        request.setLevel("debug");

        ResponseEntity<Object> response = controller.setLevel(request);

        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(Boolean.TRUE, body.get("success"));
        assertEquals("DEBUG", adapter.getLogLevel("com.foo.perm"));
    }

    @Test
    void temporarySetRollsBackAfterTtl() throws InterruptedException {
        // 记录原级别
        adapter.setLogLevel("com.foo.ttl", "INFO");

        LogLevelRequest request = new LogLevelRequest();
        request.setLogger("com.foo.ttl");
        request.setLevel("DEBUG");
        request.setTtlSeconds(1); // API 以秒为单位，1s 为最小有效 TTL

        ResponseEntity<Object> response = controller.setLevel(request);
        assertEquals(200, response.getStatusCodeValue());

        // 设置后立即生效为临时级别
        assertEquals("DEBUG", adapter.getLogLevel("com.foo.ttl"));

        // 等待到期回滚（上限 5s，避免偶发调度延迟导致 flaky）
        long deadline = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < deadline && "DEBUG".equals(adapter.getLogLevel("com.foo.ttl"))) {
            Thread.sleep(50L);
        }

        assertEquals("INFO", adapter.getLogLevel("com.foo.ttl"), "TTL 到期后应回滚到原级别");
    }

    @Test
    @SuppressWarnings("unchecked")
    void invalidLevelReturnsBadRequest() {
        LogLevelRequest request = new LogLevelRequest();
        request.setLogger("com.foo");
        request.setLevel("NOPE");

        ResponseEntity<Object> response = controller.setLevel(request);

        assertEquals(400, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(Boolean.FALSE, body.get("success"));
    }

    @Test
    void nullBodyReturnsBadRequest() {
        ResponseEntity<Object> response = controller.setLevel(null);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    void blankLoggerReturnsBadRequest() {
        LogLevelRequest request = new LogLevelRequest();
        request.setLogger("   ");
        request.setLevel("DEBUG");

        ResponseEntity<Object> response = controller.setLevel(request);

        assertEquals(400, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(body.get("message").toString().contains("logger"));
    }

    /**
     * 内存假适配器：以 Map 记录 logger→level，供 controller 直接读写。
     */
    private static final class InMemoryAdapter implements LoggingSystemAdapter {

        private final Map<String, String> levels = new ConcurrentHashMap<>();

        @Override
        public String getName() {
            return "in-memory";
        }

        @Override
        public void setLogLevel(String loggerName, String level) {
            if (level == null) {
                levels.remove(loggerName);
            } else {
                levels.put(loggerName, level);
            }
        }

        @Override
        public String getLogLevel(String loggerName) {
            return levels.get(loggerName);
        }

        @Override
        public Collection<String> getLoggerNames() {
            return new ArrayList<>(levels.keySet());
        }

        @Override
        public void resetLogLevel(String loggerName) {
            levels.remove(loggerName);
        }
    }
}
