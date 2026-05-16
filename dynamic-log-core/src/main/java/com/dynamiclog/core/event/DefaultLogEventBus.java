package com.dynamiclog.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link LogEventBus} 的默认实现。
 * <p>
 * 使用CopyOnWriteArrayList进行线程安全的监听器管理，
 * 并使用单线程ExecutorService进行异步事件发布。
 */
public class DefaultLogEventBus implements LogEventBus {

    private static final Logger log = LoggerFactory.getLogger(DefaultLogEventBus.class);

    private final List<LogEventListener> listeners = new CopyOnWriteArrayList<>();
    private final ExecutorService asyncExecutor;

    public DefaultLogEventBus() {
        this.asyncExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "dynamic-log-event-async-" + counter.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        });
    }

    @Override
    public void publish(LogEvent event) {
        for (LogEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.error("Error in event listener {} for event {}", listener.getClass().getSimpleName(), event.getType(), e);
            }
        }
    }

    @Override
    public void publishAsync(LogEvent event) {
        asyncExecutor.submit(() -> publish(event));
    }

    @Override
    public void subscribe(LogEventListener listener) {
        listeners.add(listener);
        listeners.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
    }

    @Override
    public void unsubscribe(LogEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * 关闭异步执行器。在应用程序关闭时调用。
     */
    public void shutdown() {
        asyncExecutor.shutdown();
    }
}
