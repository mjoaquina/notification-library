package com.agora.notification.validation;

import com.agora.notification.core.NotificationValidator;
import com.agora.notification.exceptions.ValidationException;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import lombok.extern.slf4j.Slf4j;

/** Validates EMAIL requests: recipient (email format), subject (required, max 255 chars), message (required, max 10000 chars). Throws ValidationException on failure. */
@Slf4j
public class EmailValidator implements NotificationValidator {
    
    private static final org.apache.commons.validator.routines.EmailValidator EMAIL_VALIDATOR = 
        org.apache.commons.validator.routines.EmailValidator.getInstance();
    private static final int MAX_SUBJECT_LENGTH = 255;
    private static final int MAX_MESSAGE_LENGTH = 10000;
    
    @Override
    public void validate(NotificationRequest request) {
        if (request == null) {
            throw new ValidationException("Notification request cannot be null");
        }
        
        if (request.getChannel() != Channel.EMAIL) {
            throw new ValidationException("Request channel must be EMAIL");
        }
        
        validateRecipient(request.getRecipient());
        validateSubject(request.getSubject());
        validateMessage(request.getMessage());
    }
    
    private void validateRecipient(String recipient) {
        if (recipient == null || recipient.isBlank()) {
            throw new ValidationException("Email recipient is required");
        }
        
        if (!EMAIL_VALIDATOR.isValid(recipient)) {
            throw new ValidationException("Invalid email format: " + recipient);
        }
    }
    
    private void validateSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new ValidationException("Email subject is required");
        }
        
        if (subject.length() > MAX_SUBJECT_LENGTH) {
            throw new ValidationException(
                String.format("Email subject exceeds maximum length of %d characters", MAX_SUBJECT_LENGTH)
            );
        }
    }
    
    private void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new ValidationException("Email message is required");
        }
        
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new ValidationException(
                String.format("Email message exceeds maximum length of %d characters", MAX_MESSAGE_LENGTH)
            );
        }
    }
    
    @Override
    public boolean supports(Channel channel) {
        return channel == Channel.EMAIL;
    }
}
