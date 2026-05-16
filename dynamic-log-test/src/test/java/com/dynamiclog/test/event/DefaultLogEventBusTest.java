package com.dynamiclog.test.event;

import com.dynamiclog.core.event.DefaultLogEventBus;
import com.dynamiclog.core.event.EventType;
import com.dynamiclog.core.event.LogEvent;
import com.dynamiclog.core.event.LogEventListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DefaultLogEventBusTest {

    private DefaultLogEventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new DefaultLogEventBus();
    }

    @AfterEach
    void tearDown() {
        eventBus.shutdown();
    }

    @Test
    void publishToSubscribers() {
        List<LogEvent> received = new ArrayList<>();
        eventBus.subscribe(received::add);

        eventBus.publish(LogEvent.builder().type(EventType.LOG_LEVEL_CHANGE).build());

        assertEquals(1, received.size());
        assertEquals(EventType.LOG_LEVEL_CHANGE, received.get(0).getType());
    }

    @Test
    void multipleSubscribersReceiveEvent() {
        List<LogEvent> received1 = new ArrayList<>();
        List<LogEvent> received2 = new ArrayList<>();
        eventBus.subscribe(received1::add);
        eventBus.subscribe(received2::add);

        eventBus.publish(LogEvent.builder().type(EventType.LOG_LEVEL_CHANGE).build());

        assertEquals(1, received1.size());
        assertEquals(1, received2.size());
    }

    @Test
    void unsubscribe() {
        List<LogEvent> received = new ArrayList<>();
        LogEventListener listener = received::add;
        eventBus.subscribe(listener);
        eventBus.unsubscribe(listener);

        eventBus.publish(LogEvent.builder().type(EventType.LOG_LEVEL_CHANGE).build());

        assertTrue(received.isEmpty());
    }

    @Test
    void publishAsync() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<LogEvent> received = new ArrayList<>();

        eventBus.subscribe(e -> {
            received.add(e);
            latch.countDown();
        });

        eventBus.publishAsync(LogEvent.builder().type(EventType.LOG_LEVEL_CHANGED).build());

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(1, received.size());
    }

    @Test
    void listenerOrderRespected() {
        List<String> order = new ArrayList<>();

        eventBus.subscribe(new LogEventListener() {
            @Override public void onEvent(LogEvent event) { order.add("second"); }
            @Override public int getOrder() { return 2; }
        });
        eventBus.subscribe(new LogEventListener() {
            @Override public void onEvent(LogEvent event) { order.add("first"); }
            @Override public int getOrder() { return 1; }
        });

        eventBus.publish(LogEvent.builder().type(EventType.LOG_LEVEL_CHANGE).build());

        assertEquals("first", order.get(0));
        assertEquals("second", order.get(1));
    }

    @Test
    void exceptionInListenerDoesNotBlockOthers() {
        List<LogEvent> received = new ArrayList<>();
        eventBus.subscribe(e -> { throw new RuntimeException("test"); });
        eventBus.subscribe(received::add);

        eventBus.publish(LogEvent.builder().type(EventType.LOG_LEVEL_CHANGE).build());

        assertEquals(1, received.size());
    }
}
