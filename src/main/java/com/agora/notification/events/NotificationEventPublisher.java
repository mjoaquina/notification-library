package com.agora.notification.events;

import com.agora.notification.core.NotificationEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Publisher for notification events using Pub/Sub pattern.
 * Manages subscribers and publishes events to all registered listeners.
 * 
 * Thread-safe implementation using CopyOnWriteArrayList for concurrent access.
 */
@Slf4j
public class NotificationEventPublisher {
    
    private final List<NotificationEventListener> listeners = new CopyOnWriteArrayList<>();
    
    /**
     * Subscribes a listener to receive notification events.
     * 
     * @param listener The listener to subscribe
     */
    public void subscribe(NotificationEventListener listener) {
        if (listener != null) {
            listeners.add(listener);
            log.debug("Subscribed listener: {}", listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Unsubscribes a listener from receiving notification events.
     * 
     * @param listener The listener to unsubscribe
     */
    public void unsubscribe(NotificationEventListener listener) {
        if (listener != null) {
            listeners.remove(listener);
            log.debug("Unsubscribed listener: {}", listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Publishes an event to all subscribed listeners.
     * 
     * @param event The event to publish
     */
    public void publish(NotificationEvent event) {
        if (event == null) {
            log.warn("Attempted to publish null event");
            return;
        }
        
        log.debug("Publishing event: {} - {}", event.getStatus(), event.getNotificationId());
        
        for (NotificationEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.error("Error notifying listener {}: {}", 
                    listener.getClass().getSimpleName(), e.getMessage(), e);
                // Continue notifying other listeners even if one fails
            }
        }
    }
    
    /**
     * Gets the number of subscribed listeners.
     * 
     * @return Number of listeners
     */
    public int getListenerCount() {
        return listeners.size();
    }
    
    /**
     * Removes all listeners.
     */
    public void clear() {
        listeners.clear();
        log.debug("Cleared all listeners");
    }
}
