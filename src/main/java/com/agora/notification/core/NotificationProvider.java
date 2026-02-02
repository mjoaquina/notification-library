package com.agora.notification.core;

import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;

/**
 * Core interface for all notification providers.
 * Each provider (SendGrid, Twilio, FCM, etc.) must implement this interface.
 * 
 * This follows the Strategy pattern - different providers can be swapped
 * transparently without changing client code.
 */
public interface NotificationProvider {
    
    /**
     * Sends a notification using this provider.
     * 
     * @param request The notification request containing all necessary data
     * @return NotificationResult indicating success or failure
     * @throws com.agora.notification.exceptions.ProviderException if the provider fails
     */
    NotificationResult send(NotificationRequest request);
    
    /**
     * Returns the name of this provider (e.g., "SendGrid", "Twilio").
     * 
     * @return Provider name
     */
    String getName();
    
    /**
     * Validates that the provider is properly configured.
     * 
     * @return true if the provider is ready to send notifications
     */
    boolean isConfigured();
}
