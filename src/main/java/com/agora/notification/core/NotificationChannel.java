package com.agora.notification.core;

import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;

/**
 * Interface for notification channels (Email, SMS, Push).
 * Each channel manages multiple providers and can switch between them.
 * 
 * This abstraction allows the system to work with channels independently 
 * of the specific provider implementation.
 */
public interface NotificationChannel {
    
    /**
     * Sends a notification through this channel using the configured provider.
     * 
     * @param request The notification request
     * @return NotificationResult indicating success or failure
     */
    NotificationResult send(NotificationRequest request);
    
    /**
     * Returns the channel type this implementation handles.
     * 
     * @return The channel type
     */
    Channel getChannelType();
    
    /**
     * Sets the active provider for this channel.
     * 
     * @param provider The provider to use
     */
    void setProvider(NotificationProvider provider);
    
    /**
     * Gets the currently active provider.
     * 
     * @return The active provider, or null if none is set
     */
    NotificationProvider getProvider();
}
