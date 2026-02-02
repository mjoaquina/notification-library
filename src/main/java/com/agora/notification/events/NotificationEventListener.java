package com.agora.notification.events;

import com.agora.notification.core.NotificationEvent;

/**
 * Interface for listening to notification events.
 * Implementations can subscribe to receive notifications about
 * notification lifecycle events (PENDING, SENT, FAILED, RETRYING).
 * 
 */
public interface NotificationEventListener {
    
    /**
     * Called when a notification event occurs.
     * 
     * @param event The notification event
     */
    void onEvent(NotificationEvent event);
}
