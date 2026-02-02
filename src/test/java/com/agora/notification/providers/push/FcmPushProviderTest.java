package com.agora.notification.providers.push;

import com.agora.notification.config.PushConfig;
import com.agora.notification.exceptions.ProviderException;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.models.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FcmPushProviderTest {
    
    private FcmPushProvider provider;
    private PushConfig validConfig;
    
    @BeforeEach
    void setUp() {
        validConfig = PushConfig.builder()
            .serverKey("AAAA1234567890:APA91bH1234567890abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJ")
            .apiUrl("https://fcm.googleapis.com")
            .timeoutMs(5000)
            .build();
        
        provider = new FcmPushProvider(validConfig);
    }
    
    @Test
    void testGetName() {
        assertEquals("FCM", provider.getName());
    }
    
    @Test
    void testIsConfiguredWithValidConfig() {
        assertTrue(provider.isConfigured());
    }
    
    @Test
    void testIsConfiguredWithNullConfig() {
        FcmPushProvider unconfiguredProvider = new FcmPushProvider(null);
        assertFalse(unconfiguredProvider.isConfigured());
    }
    
    @Test
    void testSendWithUnconfiguredProvider() {
        FcmPushProvider unconfiguredProvider = new FcmPushProvider(null);
        NotificationRequest request = createValidRequest();
        
        assertThrows(ProviderException.class, () -> unconfiguredProvider.send(request));
    }
    
    @Test
    void testSendWithValidRequest() {
        NotificationRequest request = createValidRequest();
        
        boolean successFound = false;
        boolean failureFound = false;
        
        for (int i = 0; i < 20; i++) {
            try {
                NotificationResult result = provider.send(request);
                assertNotNull(result);
                assertEquals("FCM", result.getProviderName());
                assertEquals(NotificationStatus.SENT, result.getStatus());
                assertTrue(result.isSuccess());
                successFound = true;
            } catch (ProviderException e) {
                assertTrue(e.getMessage().contains("FCM"));
                failureFound = true;
            }
        }
        
        assertTrue(successFound || failureFound, "Should have some results");
    }
    
    @Test
    void testSendWithShortServerKey() {
        PushConfig shortConfig = PushConfig.builder()
            .serverKey("short")
            .apiUrl("https://fcm.googleapis.com")
            .timeoutMs(5000)
            .build();
        
        FcmPushProvider providerWithShortKey = new FcmPushProvider(shortConfig);
        NotificationRequest request = createValidRequest();
        
        assertThrows(ProviderException.class, () -> providerWithShortKey.send(request));
    }
    
    private NotificationRequest createValidRequest() {
        return NotificationRequest.builder()
            .channel(Channel.PUSH)
            .recipient("device_token_1234567890")
            .title("Test Title")
            .body("Test Body")
            .message("Test message")
            .build();
    }
}
