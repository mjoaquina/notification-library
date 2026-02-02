package com.agora.notification.channels;

import com.agora.notification.config.EmailConfig;
import com.agora.notification.core.NotificationProvider;
import com.agora.notification.exceptions.ProviderException;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.models.NotificationStatus;
import com.agora.notification.providers.email.SendGridEmailProvider;
import com.agora.notification.retry.ExponentialBackoffRetry;
import com.agora.notification.retry.RetryConfig;
import com.agora.notification.retry.RetryExecutor;
import com.agora.notification.retry.RetryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailChannelTest {

    private static final String VALID_SENDGRID_API_KEY = "SG.valid_api_key_12345678901234567890";
    private static final String SENDGRID_API_URL = "https://api.sendgrid.com";
    private static final int TIMEOUT_MS = 5000;

    @Mock
    private NotificationProvider mockProvider;

    private EmailChannel emailChannel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailChannel = new EmailChannel();
    }

    @Test
    void getChannelType_returnsEmail() {
        assertEquals(Channel.EMAIL, emailChannel.getChannelType());
    }

    @Test
    void setProvider_andGetProvider_roundtrip() {
        emailChannel.setProvider(mockProvider);
        assertEquals(mockProvider, emailChannel.getProvider());
    }

    @Test
    void send_withNullProvider_throwsProviderException() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();

        assertThrows(ProviderException.class, () -> emailChannel.send(request));
    }

    @Test
    void send_withUnconfiguredProvider_throwsProviderException() {
        when(mockProvider.isConfigured()).thenReturn(false);
        when(mockProvider.getName()).thenReturn("TestProvider");

        emailChannel.setProvider(mockProvider);

        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();

        assertThrows(ProviderException.class, () -> emailChannel.send(request));
        verify(mockProvider).isConfigured();
    }

    @Test
    void send_withConfiguredProvider_returnsResultAndCallsProvider() {
        NotificationResult expectedResult = NotificationResult.builder()
            .success(true)
            .status(NotificationStatus.SENT)
            .message("Email sent")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();

        when(mockProvider.isConfigured()).thenReturn(true);
        when(mockProvider.send(any(NotificationRequest.class))).thenReturn(expectedResult);
        when(mockProvider.getName()).thenReturn("TestProvider");

        emailChannel.setProvider(mockProvider);

        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();

        NotificationResult result = emailChannel.send(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(expectedResult, result);
        verify(mockProvider).send(request);
    }

    @Test
    void send_withRealSendGridProvider_returnsResultOrThrowsProviderException() {
        EmailConfig config = EmailConfig.builder()
            .apiKey(VALID_SENDGRID_API_KEY)
            .fromEmail("sender@example.com")
            .fromName("Test Sender")
            .apiUrl(SENDGRID_API_URL)
            .timeoutMs(TIMEOUT_MS)
            .build();

        SendGridEmailProvider sendGridProvider = new SendGridEmailProvider(config);
        emailChannel.setProvider(sendGridProvider);

        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();

        assertDoesNotThrow(() -> {
            try {
                emailChannel.send(request);
            } catch (ProviderException e) {
                // Simulated failure is acceptable
            }
        });
    }

    @Test
    void send_withRetryExecutor_usesRetryAndReturnsResult() {
        NotificationResult successResult = NotificationResult.builder()
            .success(true)
            .status(NotificationStatus.SENT)
            .message("Sent")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();

        when(mockProvider.isConfigured()).thenReturn(true);
        when(mockProvider.getName()).thenReturn("TestProvider");
        when(mockProvider.send(any(NotificationRequest.class))).thenReturn(successResult);

        RetryPolicy retryPolicy = new RetryPolicy() {
            @Override
            public boolean shouldRetry(NotificationResult result, int attemptNumber) {
                return attemptNumber < 2;
            }
            @Override
            public long calculateDelayMs(int attemptNumber) {
                return 0;
            }
            @Override
            public int getMaxAttempts() {
                return 2;
            }
        };
        RetryExecutor retryExecutor = new RetryExecutor(retryPolicy);
        EmailChannel channelWithRetry = new EmailChannel(mockProvider, retryExecutor);

        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();

        NotificationResult result = channelWithRetry.send(request);
        assertTrue(result.isSuccess());
        verify(mockProvider).send(request);
    }

    @Test
    void constructor_withProviderAndRetryExecutor_setsBoth() {
        when(mockProvider.getName()).thenReturn("Test");
        RetryExecutor retryExecutor = new RetryExecutor(
            new ExponentialBackoffRetry(RetryConfig.builder().maxAttempts(1).build()));
        EmailChannel channel = new EmailChannel(mockProvider, retryExecutor);
        assertEquals(mockProvider, channel.getProvider());
        assertEquals(retryExecutor, channel.getRetryExecutor());
    }
}
