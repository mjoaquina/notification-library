package com.agora.notification.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PushConfigTest {
    
    @Test
    void testValidConfigForFcm() {
        PushConfig config = PushConfig.builder()
            .serverKey("AAAA1234567890:APA91bH1234567890abcdefghijklmnopqrstuvwxyz1234567890")
            .apiUrl("https://fcm.googleapis.com")
            .timeoutMs(5000)
            .build();
        
        assertTrue(config.isValidForFcm());
        assertFalse(config.isValidForOneSignal());
    }
    
    @Test
    void testValidConfigForOneSignal() {
        PushConfig config = PushConfig.builder()
            .apiKey("12345678-1234-1234-1234-123456789012")
            .appId("12345678-1234-1234-1234-123456789012")
            .apiUrl("https://onesignal.com/api/v1")
            .timeoutMs(5000)
            .build();
        
        assertTrue(config.isValidForOneSignal());
        assertFalse(config.isValidForFcm());
    }
    
    @Test
    void testInvalidFcmConfigWithNullServerKey() {
        PushConfig config = PushConfig.builder()
            .serverKey(null)
            .apiUrl("https://fcm.googleapis.com")
            .build();
        
        assertFalse(config.isValidForFcm());
    }
    
    @Test
    void testInvalidOneSignalConfigWithNullAppId() {
        PushConfig config = PushConfig.builder()
            .apiKey("key")
            .appId(null)
            .apiUrl("https://onesignal.com")
            .build();
        
        assertFalse(config.isValidForOneSignal());
    }
}
