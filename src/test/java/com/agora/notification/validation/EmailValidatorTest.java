package com.agora.notification.validation;

import com.agora.notification.exceptions.ValidationException;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    
    private EmailValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new EmailValidator();
    }
    
    @Test
    void testSupportsEmailChannel() {
        assertTrue(validator.supports(Channel.EMAIL));
        assertFalse(validator.supports(Channel.SMS));
        assertFalse(validator.supports(Channel.PUSH));
    }
    
    @Test
    void testValidateNullRequest() {
        assertThrows(ValidationException.class, () -> validator.validate(null));
    }
    
    @Test
    void testValidateWrongChannel() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateNullRecipient() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient(null)
            .subject("Test")
            .message("Test message")
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateBlankRecipient() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("   ")
            .subject("Test")
            .message("Test message")
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateInvalidEmailFormat() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("invalid-email")
            .subject("Test")
            .message("Test message")
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateNullSubject() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject(null)
            .message("Test message")
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateBlankSubject() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("   ")
            .message("Test message")
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateSubjectTooLong() {
        String longSubject = "a".repeat(256);
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject(longSubject)
            .message("Test message")
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateNullMessage() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message(null)
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateBlankMessage() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("   ")
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateMessageTooLong() {
        String longMessage = "a".repeat(10001);
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message(longMessage)
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateValidRequest() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test Subject")
            .message("Test message content")
            .build();
        
        assertDoesNotThrow(() -> validator.validate(request));
    }
    
    @Test
    void testValidateValidEmailFormats() {
        String[] validEmails = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user123@example-domain.com"
        };
        
        for (String email : validEmails) {
            NotificationRequest request = NotificationRequest.builder()
                .channel(Channel.EMAIL)
                .recipient(email)
                .subject("Test")
                .message("Test message")
                .build();
            
            assertDoesNotThrow(() -> validator.validate(request), 
                "Should accept valid email: " + email);
        }
    }
}
