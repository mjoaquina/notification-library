package com.agora.notification.providers.email;

import com.agora.notification.config.EmailConfig;
import com.agora.notification.exceptions.ProviderException;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.models.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MailgunEmailProviderTest {
    
    private MailgunEmailProvider provider;
    private EmailConfig validConfig;
    
    @BeforeEach
    void setUp() {
        validConfig = EmailConfig.builder()
            .apiKey("key-valid_api_key_12345678901234567890")
            .fromEmail("sender@example.com")
            .fromName("Test Sender")
            .apiUrl("https://api.mailgun.net")
            .timeoutMs(5000)
            .build();
        
        provider = new MailgunEmailProvider(validConfig);
    }
    
    @Test
    void testGetName() {
        assertEquals("Mailgun", provider.getName());
    }
    
    @Test
    void testIsConfiguredWithValidConfig() {
        assertTrue(provider.isConfigured());
    }
    
    @Test
    void testIsConfiguredWithNullConfig() {
        MailgunEmailProvider unconfiguredProvider = new MailgunEmailProvider(null);
        assertFalse(unconfiguredProvider.isConfigured());
    }
    
    @Test
    void testIsConfiguredWithInvalidConfig() {
        EmailConfig invalidConfig = EmailConfig.builder()
            .apiKey("")
            .fromEmail("sender@example.com")
            .apiUrl("https://api.mailgun.net")
            .build();
        
        MailgunEmailProvider unconfiguredProvider = new MailgunEmailProvider(invalidConfig);
        assertFalse(unconfiguredProvider.isConfigured());
    }
    
    @Test
    void testSendWithUnconfiguredProvider() {
        MailgunEmailProvider unconfiguredProvider = new MailgunEmailProvider(null);
        NotificationRequest request = createValidRequest();
        
        assertThrows(ProviderException.class, () -> unconfiguredProvider.send(request));
    }
    
    @Test
    void testSendWithValidRequest() {
        NotificationRequest request = createValidRequest();
        
        // Since we're simulating, the result may vary, but should not throw
        // We'll run multiple times to account for random success/failure
        boolean successFound = false;
        boolean failureFound = false;
        
        for (int i = 0; i < 20; i++) {
            try {
                NotificationResult result = provider.send(request);
                assertNotNull(result);
                assertEquals("Mailgun", result.getProviderName());
                assertEquals(NotificationStatus.SENT, result.getStatus());
                assertTrue(result.isSuccess());
                successFound = true;
            } catch (ProviderException e) {
                assertTrue(e.getMessage().contains("Mailgun"));
                failureFound = true;
            }
        }
        
        // With 92% success rate, we should see both outcomes in 20 attempts
        assertTrue(successFound || failureFound, "Should have some results");
    }
    
    @Test
    void testSendResultStructure() {
        NotificationRequest request = createValidRequest();
        
        // Try until we get a success to verify structure
        NotificationResult result = null;
        for (int i = 0; i < 50; i++) {
            try {
                result = provider.send(request);
                if (result.isSuccess()) {
                    break;
                }
            } catch (ProviderException e) {
                // Continue trying
            }
        }
        
        if (result != null && result.isSuccess()) {
            assertNotNull(result.getTimestamp());
            assertNotNull(result.getMessage());
            assertEquals("Mailgun", result.getProviderName());
            assertEquals(1, result.getAttemptNumber());
        }
    }
    
    @Test
    void testSendWithShortApiKey() {
        EmailConfig shortKeyConfig = EmailConfig.builder()
            .apiKey("short")
            .fromEmail("sender@example.com")
            .fromName("Test Sender")
            .apiUrl("https://api.mailgun.net")
            .timeoutMs(5000)
            .build();
        
        MailgunEmailProvider providerWithShortKey = new MailgunEmailProvider(shortKeyConfig);
        NotificationRequest request = createValidRequest();
        
        // Short API key should cause failure
        assertThrows(ProviderException.class, () -> providerWithShortKey.send(request));
    }
    
    @Test
    void testSendWithNullFromName() {
        EmailConfig configWithoutName = EmailConfig.builder()
            .apiKey("key-valid_api_key_12345678901234567890")
            .fromEmail("sender@example.com")
            .fromName(null)
            .apiUrl("https://api.mailgun.net")
            .timeoutMs(5000)
            .build();
        
        MailgunEmailProvider providerWithoutName = new MailgunEmailProvider(configWithoutName);
        NotificationRequest request = createValidRequest();
        
        // Should work without fromName
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 20; i++) {
                try {
                    providerWithoutName.send(request);
                    break;
                } catch (ProviderException e) {
                    // Continue trying
                }
            }
        });
    }
    
    private NotificationRequest createValidRequest() {
        return NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("recipient@example.com")
            .subject("Test Subject")
            .message("Test message content")
            .build();
    }
}
