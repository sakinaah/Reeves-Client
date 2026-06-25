package com.reevesclient.core.event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Simple, lightweight publish-subscribe event bus.
 * Subscribers register typed consumers; publishers post event instances.
 * Thread-safe for concurrent registration but events are dispatched on the caller's thread.
 */
public class EventBus {

    private static final EventBus INSTANCE = new EventBus();

    private final Map<Class<?>, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    private EventBus() {}

    public static EventBus getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> Collections.synchronizedList(new ArrayList<>()))
                 .add((Consumer<Object>) listener);
    }

    @SuppressWarnings("unchecked")
    public <T> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        List<Consumer<Object>> list = listeners.get(eventType);
        if (list != null) {
            list.remove(listener);
        }
    }

    public <T> void post(T event) {
        List<Consumer<Object>> list = listeners.get(event.getClass());
        if (list == null || list.isEmpty()) return;
        for (Consumer<Object> consumer : list) {
            try {
                consumer.accept(event);
            } catch (Exception e) {
                com.reevesclient.ReevesClient.LOGGER.error(
                        "Exception in event listener for {}: {}", event.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }
}
