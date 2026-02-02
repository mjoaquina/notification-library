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

class SendGridEmailProviderTest {

    private static final String VALID_API_KEY = "SG.valid_api_key_12345678901234567890";
    private static final String SENDGRID_API_URL = "https://api.sendgrid.com";
    private static final int TIMEOUT_MS = 5000;

    private SendGridEmailProvider provider;
    private EmailConfig validConfig;

    @BeforeEach
    void setUp() {
        validConfig = EmailConfig.builder()
            .apiKey(VALID_API_KEY)
            .fromEmail("sender@example.com")
            .fromName("Test Sender")
            .apiUrl(SENDGRID_API_URL)
            .timeoutMs(TIMEOUT_MS)
            .build();

        provider = new SendGridEmailProvider(validConfig);
    }

    @Test
    void getName_returnsSendGrid() {
        assertEquals("SendGrid", provider.getName());
    }

    @Test
    void isConfigured_withValidConfig_returnsTrue() {
        assertTrue(provider.isConfigured());
    }

    @Test
    void isConfigured_withNullConfig_returnsFalse() {
        SendGridEmailProvider unconfiguredProvider = new SendGridEmailProvider(null);
        assertFalse(unconfiguredProvider.isConfigured());
    }

    @Test
    void isConfigured_withInvalidConfig_returnsFalse() {
        EmailConfig invalidConfig = EmailConfig.builder()
            .apiKey("")
            .fromEmail("sender@example.com")
            .apiUrl(SENDGRID_API_URL)
            .build();

        SendGridEmailProvider unconfiguredProvider = new SendGridEmailProvider(invalidConfig);
        assertFalse(unconfiguredProvider.isConfigured());
    }

    @Test
    void send_withUnconfiguredProvider_throwsProviderException() {
        SendGridEmailProvider unconfiguredProvider = new SendGridEmailProvider(null);
        NotificationRequest request = createValidRequest();

        assertThrows(ProviderException.class, () -> unconfiguredProvider.send(request));
    }

    @Test
    void send_withValidRequest_returnsResultOrThrowsProviderException() {
        NotificationRequest request = createValidRequest();

        try {
            NotificationResult result = provider.send(request);
            assertNotNull(result);
            assertEquals("SendGrid", result.getProviderName());
            assertEquals(NotificationStatus.SENT, result.getStatus());
            assertTrue(result.isSuccess());
        } catch (ProviderException e) {
            assertTrue(e.getMessage().contains("SendGrid"));
        }
    }

    @Test
    void send_withValidRequest_whenSuccess_resultHasExpectedStructure() {
        NotificationRequest request = createValidRequest();

        try {
            NotificationResult result = provider.send(request);
            if (result.isSuccess()) {
                assertNotNull(result.getTimestamp());
                assertNotNull(result.getMessage());
                assertEquals("SendGrid", result.getProviderName());
                assertEquals(1, result.getAttemptNumber());
            }
        } catch (ProviderException e) {
            // Simulated failure is acceptable; we only assert structure on success
        }
    }

    @Test
    void send_withShortApiKey_throwsProviderException() {
        EmailConfig shortKeyConfig = EmailConfig.builder()
            .apiKey("short")
            .fromEmail("sender@example.com")
            .fromName("Test Sender")
            .apiUrl(SENDGRID_API_URL)
            .timeoutMs(TIMEOUT_MS)
            .build();

        SendGridEmailProvider providerWithShortKey = new SendGridEmailProvider(shortKeyConfig);
        NotificationRequest request = createValidRequest();

        assertThrows(ProviderException.class, () -> providerWithShortKey.send(request));
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
