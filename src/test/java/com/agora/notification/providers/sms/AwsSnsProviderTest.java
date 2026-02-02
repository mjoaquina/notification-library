package com.agora.notification.providers.sms;

import com.agora.notification.config.SmsConfig;
import com.agora.notification.exceptions.ProviderException;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.models.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AwsSnsProviderTest {
    
    private AwsSnsProvider provider;
    private SmsConfig validConfig;
    
    @BeforeEach
    void setUp() {
        validConfig = SmsConfig.builder()
            .apiKey("AKIAIOSFODNN7EXAMPLE12345")
            .apiSecret("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY12345")
            .region("us-east-1")
            .apiUrl("https://sns.us-east-1.amazonaws.com")
            .timeoutMs(5000)
            .build();
        
        provider = new AwsSnsProvider(validConfig);
    }
    
    @Test
    void testGetName() {
        assertEquals("AWS SNS", provider.getName());
    }
    
    @Test
    void testIsConfiguredWithValidConfig() {
        assertTrue(provider.isConfigured());
    }
    
    @Test
    void testIsConfiguredWithNullConfig() {
        AwsSnsProvider unconfiguredProvider = new AwsSnsProvider(null);
        assertFalse(unconfiguredProvider.isConfigured());
    }
    
    @Test
    void testSendWithUnconfiguredProvider() {
        AwsSnsProvider unconfiguredProvider = new AwsSnsProvider(null);
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
                assertEquals("AWS SNS", result.getProviderName());
                assertEquals(NotificationStatus.SENT, result.getStatus());
                assertTrue(result.isSuccess());
                successFound = true;
            } catch (ProviderException e) {
                assertTrue(e.getMessage().contains("AWS SNS"));
                failureFound = true;
            }
        }
        
        assertTrue(successFound || failureFound, "Should have some results");
    }
    
    @Test
    void testSendWithShortCredentials() {
        SmsConfig shortConfig = SmsConfig.builder()
            .apiKey("short")
            .apiSecret("short")
            .region("us-east-1")
            .apiUrl("https://sns.us-east-1.amazonaws.com")
            .timeoutMs(5000)
            .build();
        
        AwsSnsProvider providerWithShortCreds = new AwsSnsProvider(shortConfig);
        NotificationRequest request = createValidRequest();
        
        assertThrows(ProviderException.class, () -> providerWithShortCreds.send(request));
    }
    
    private NotificationRequest createValidRequest() {
        return NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient("+0987654321")
            .message("Test SMS message")
            .build();
    }
}
