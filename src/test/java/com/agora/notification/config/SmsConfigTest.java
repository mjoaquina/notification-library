package com.agora.notification.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmsConfigTest {
    
    @Test
    void isValidForTwilio_withTwilioFields_returnsTrue() {
        SmsConfig config = SmsConfig.builder()
            .accountSid("test-twilio-account-sid")
            .authToken("test-auth-token")
            .fromNumber("+1234567890")
            .apiUrl("https://api.twilio.com")
            .timeoutMs(5000)
            .build();
        
        assertTrue(config.isValidForTwilio());
        assertFalse(config.isValidForAwsSns());
    }
    
    @Test
    void isValidForAwsSns_withAwsSnsFields_returnsTrue() {
        SmsConfig config = SmsConfig.builder()
            .apiKey("AKIAIOSFODNN7EXAMPLE")
            .apiSecret("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
            .region("us-east-1")
            .apiUrl("https://sns.us-east-1.amazonaws.com")
            .timeoutMs(5000)
            .build();
        
        assertTrue(config.isValidForAwsSns());
        assertFalse(config.isValidForTwilio());
    }
    
    @Test
    void isValidForTwilio_withNullAccountSid_returnsFalse() {
        SmsConfig config = SmsConfig.builder()
            .accountSid(null)
            .authToken("auth_token")
            .fromNumber("+1234567890")
            .apiUrl("https://api.twilio.com")
            .build();
        
        assertFalse(config.isValidForTwilio());
    }
    
    @Test
    void isValidForAwsSns_withNullApiKey_returnsFalse() {
        SmsConfig config = SmsConfig.builder()
            .apiKey(null)
            .apiSecret("secret")
            .region("us-east-1")
            .apiUrl("https://sns.us-east-1.amazonaws.com")
            .build();
        
        assertFalse(config.isValidForAwsSns());
    }
}
