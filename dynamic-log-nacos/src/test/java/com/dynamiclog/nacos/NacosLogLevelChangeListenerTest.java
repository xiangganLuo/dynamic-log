package com.dynamiclog.nacos;

import com.dynamiclog.spring.refresher.SpringEnvironmentRefresher;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 验证 {@link NacosLogLevelChangeListener} 已真正接通 Spring Cloud 的
 * {@link EnvironmentChangeEvent} 到 {@link SpringEnvironmentRefresher}
 * （此前该链路为未接线的死代码）。
 * <p>
 * 使用 {@link CountingRefresher} 计数子类替代 mock（规避 mockito-inline 在 JDK 21 上
 * retransform 具体类的限制）：{@code SpringEnvironmentRefresher} 构造仅赋值，
 * 覆写 {@code refresh()} 即可安全计数。
 */
class NacosLogLevelChangeListenerTest {

    @Test
    void refreshesWhenLoggingLevelKeyChanged() {
        CountingRefresher refresher = new CountingRefresher();
        NacosLogLevelChangeListener listener = new NacosLogLevelChangeListener(refresher);

        Set<String> keys = new HashSet<>();
        keys.add("logging.level.com.foo");
        keys.add("server.port");

        listener.onApplicationEvent(new EnvironmentChangeEvent(keys));

        assertEquals(1, refresher.count);
    }

    @Test
    void doesNotRefreshWhenNoLoggingKeys() {
        CountingRefresher refresher = new CountingRefresher();
        NacosLogLevelChangeListener listener = new NacosLogLevelChangeListener(refresher);

        listener.onApplicationEvent(new EnvironmentChangeEvent(Collections.singleton("server.port")));

        assertEquals(0, refresher.count);
    }

    @Test
    void doesNotRefreshWhenKeysEmpty() {
        CountingRefresher refresher = new CountingRefresher();
        NacosLogLevelChangeListener listener = new NacosLogLevelChangeListener(refresher);

        listener.onApplicationEvent(new EnvironmentChangeEvent(Collections.<String>emptySet()));

        assertEquals(0, refresher.count);
    }

    /**
     * 计数用测试替身：父类构造仅赋值，故传 null 安全；覆写 refresh 统计调用次数。
     */
    private static final class CountingRefresher extends SpringEnvironmentRefresher {

        private int count = 0;

        CountingRefresher() {
            super(null, null, null);
        }

        @Override
        public void refresh() {
            count++;
        }
    }
}
