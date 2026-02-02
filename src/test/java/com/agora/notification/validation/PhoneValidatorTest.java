package com.agora.notification.validation;

import com.agora.notification.exceptions.ValidationException;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneValidatorTest {
    
    private PhoneValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new PhoneValidator();
    }
    
    @Test
    void testSupportsSmsChannel() {
        assertTrue(validator.supports(Channel.SMS));
        assertFalse(validator.supports(Channel.EMAIL));
        assertFalse(validator.supports(Channel.PUSH));
    }
    
    @Test
    void testValidateNullRequest() {
        assertThrows(ValidationException.class, () -> validator.validate(null));
    }
    
    @Test
    void testValidateWrongChannel() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("+1234567890")
            .message("Test message")
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateNullRecipient() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient(null)
            .message("Test message")
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateBlankRecipient() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient("   ")
            .message("Test message")
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateInvalidPhoneFormat() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient("invalid-phone")
            .message("Test message")
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateNullMessage() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient("+1234567890")
            .message(null)
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateBlankMessage() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient("+1234567890")
            .message("   ")
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateMessageTooLong() {
        String longMessage = "a".repeat(1601);
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient("+1234567890")
            .message(longMessage)
            .build();
        
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
    
    @Test
    void testValidateValidRequest() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient("+1234567890")
            .message("Test message")
            .build();
        
        assertDoesNotThrow(() -> validator.validate(request));
    }
    
    @Test
    void testValidateValidPhoneFormats() {
        String[] validPhones = {
            "+1234567890",
            "+12345678901234",
            "1234567890",
            "+1-234-567-8900",
            "+1 (234) 567-8900",
            "+1234567890123"
        };
        
        for (String phone : validPhones) {
            NotificationRequest request = NotificationRequest.builder()
                .channel(Channel.SMS)
                .recipient(phone)
                .message("Test message")
                .build();
            
            assertDoesNotThrow(() -> validator.validate(request), 
                "Should accept valid phone: " + phone);
        }
    }
    
    @Test
    void testValidateInvalidPhoneFormats() {
        String[] invalidPhones = {
            "123", // Too short
            "abc123", // Contains letters
            "+", // Just plus sign
            "", // Empty
            "12345" // Too short
        };
        
        for (String phone : invalidPhones) {
            NotificationRequest request = NotificationRequest.builder()
                .channel(Channel.SMS)
                .recipient(phone)
                .message("Test message")
                .build();
            
            assertThrows(ValidationException.class, () -> validator.validate(request),
                "Should reject invalid phone: " + phone);
        }
    }
}
