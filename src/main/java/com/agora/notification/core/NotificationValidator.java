package com.agora.notification.core;

import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.Channel;

/**
 * Interface for validating notification requests.
 * Different validators can be created for different channels or validation rules.
 * 
 * Follows Single Responsibility Principle - validation logic is separated
 * from sending logic.
 */
public interface NotificationValidator {
    
    /**
     * Validates a notification request.
     * 
     * @param request The request to validate
     * @throws com.agora.notification.exceptions.ValidationException if validation fails
     */
    void validate(NotificationRequest request);
    
    /**
     * Checks if this validator supports the given channel.
     * 
     * @param channel The channel to check
     * @return true if this validator can validate requests for this channel
     */
    boolean supports(Channel channel);
}
