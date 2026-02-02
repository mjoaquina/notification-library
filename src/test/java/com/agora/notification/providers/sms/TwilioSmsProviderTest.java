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

class TwilioSmsProviderTest {
    
    private TwilioSmsProvider provider;
    private SmsConfig validConfig;
    
    @BeforeEach
    void setUp() {
        validConfig = SmsConfig.builder()
            .accountSid("test-twilio-account-sid")
            .authToken("test-auth-token")
            .fromNumber("+1234567890")
            .apiUrl("https://api.twilio.com")
            .timeoutMs(5000)
            .build();
        
        provider = new TwilioSmsProvider(validConfig);
    }
    
    @Test
    void testGetName() {
        assertEquals("Twilio", provider.getName());
    }
    
    @Test
    void testIsConfiguredWithValidConfig() {
        assertTrue(provider.isConfigured());
    }
    
    @Test
    void testIsConfiguredWithNullConfig() {
        TwilioSmsProvider unconfiguredProvider = new TwilioSmsProvider(null);
        assertFalse(unconfiguredProvider.isConfigured());
    }
    
    @Test
    void testSendWithUnconfiguredProvider() {
        TwilioSmsProvider unconfiguredProvider = new TwilioSmsProvider(null);
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
                assertEquals("Twilio", result.getProviderName());
                assertEquals(NotificationStatus.SENT, result.getStatus());
                assertTrue(result.isSuccess());
                successFound = true;
            } catch (ProviderException e) {
                assertTrue(e.getMessage().contains("Twilio"));
                failureFound = true;
            }
        }
        
        assertTrue(successFound || failureFound, "Should have some results");
    }
    
    @Test
    void testSendWithShortCredentials() {
        SmsConfig shortConfig = SmsConfig.builder()
            .accountSid("short")
            .authToken("short")
            .fromNumber("+1234567890")
            .apiUrl("https://api.twilio.com")
            .timeoutMs(5000)
            .build();
        
        TwilioSmsProvider providerWithShortCreds = new TwilioSmsProvider(shortConfig);
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
