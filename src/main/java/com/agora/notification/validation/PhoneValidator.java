package com.agora.notification.validation;

import com.agora.notification.core.NotificationValidator;
import com.agora.notification.exceptions.ValidationException;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/** Validates SMS requests: recipient (E.164-like, 7–15 digits), message (required, max 1600 chars). Throws ValidationException on failure. */
@Slf4j
public class PhoneValidator implements NotificationValidator {
    
    // E.164 format: +[country code][number] (e.g., +1234567890)
    // Also accept numbers without + (for flexibility)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[1-9]\\d{6,14}$");
    private static final int MAX_MESSAGE_LENGTH = 1600; // SMS character limit
    
    @Override
    public void validate(NotificationRequest request) {
        if (request == null) {
            throw new ValidationException("Notification request cannot be null");
        }
        
        if (request.getChannel() != Channel.SMS) {
            throw new ValidationException("Request channel must be SMS");
        }
        
        validateRecipient(request.getRecipient());
        validateMessage(request.getMessage());
    }
    
    private void validateRecipient(String recipient) {
        if (recipient == null || recipient.isBlank()) {
            throw new ValidationException("SMS recipient phone number is required");
        }
        
        // Remove common formatting characters for validation
        String cleaned = recipient.replaceAll("[\\s\\-\\(\\)]", "");
        
        if (!PHONE_PATTERN.matcher(cleaned).matches()) {
            throw new ValidationException("Invalid phone number format: " + recipient + 
                ". Expected format: +[country code][number] or [country code][number]");
        }
    }
    
    private void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new ValidationException("SMS message is required");
        }
        
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new ValidationException(
                String.format("SMS message exceeds maximum length of %d characters", MAX_MESSAGE_LENGTH)
            );
        }
    }
    
    @Override
    public boolean supports(Channel channel) {
        return channel == Channel.SMS;
    }
}
