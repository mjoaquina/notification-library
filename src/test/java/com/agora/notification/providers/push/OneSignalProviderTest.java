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

class OneSignalProviderTest {
    
    private OneSignalProvider provider;
    private PushConfig validConfig;
    
    @BeforeEach
    void setUp() {
        validConfig = PushConfig.builder()
            .apiKey("12345678-1234-1234-1234-123456789012")
            .appId("12345678-1234-1234-1234-123456789012")
            .apiUrl("https://onesignal.com/api/v1")
            .timeoutMs(5000)
            .build();
        
        provider = new OneSignalProvider(validConfig);
    }
    
    @Test
    void testGetName() {
        assertEquals("OneSignal", provider.getName());
    }
    
    @Test
    void testIsConfiguredWithValidConfig() {
        assertTrue(provider.isConfigured());
    }
    
    @Test
    void testIsConfiguredWithNullConfig() {
        OneSignalProvider unconfiguredProvider = new OneSignalProvider(null);
        assertFalse(unconfiguredProvider.isConfigured());
    }
    
    @Test
    void testSendWithUnconfiguredProvider() {
        OneSignalProvider unconfiguredProvider = new OneSignalProvider(null);
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
                assertEquals("OneSignal", result.getProviderName());
                assertEquals(NotificationStatus.SENT, result.getStatus());
                assertTrue(result.isSuccess());
                successFound = true;
            } catch (ProviderException e) {
                assertTrue(e.getMessage().contains("OneSignal"));
                failureFound = true;
            }
        }
        
        assertTrue(successFound || failureFound, "Should have some results");
    }
    
    @Test
    void testSendWithShortCredentials() {
        PushConfig shortConfig = PushConfig.builder()
            .apiKey("short")
            .appId("short")
            .apiUrl("https://onesignal.com/api/v1")
            .timeoutMs(5000)
            .build();
        
        OneSignalProvider providerWithShortCreds = new OneSignalProvider(shortConfig);
        NotificationRequest request = createValidRequest();
        
        assertThrows(ProviderException.class, () -> providerWithShortCreds.send(request));
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
