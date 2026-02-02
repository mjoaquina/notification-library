package com.agora.notification.channels;

import com.agora.notification.core.NotificationProvider;
import com.agora.notification.core.RetryableChannel;
import com.agora.notification.exceptions.ProviderException;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.retry.RetryExecutor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Push notification channel implementation. Delegates to the configured {@link NotificationProvider};
 * supports optional retry via {@link RetryExecutor}. Use with FCM, OneSignal, or custom
 * push providers. Implements {@link RetryableChannel} for retry configuration from the service builder.
 */
@Slf4j
public class PushChannel implements RetryableChannel {

    @Getter
    @Setter
    private NotificationProvider provider;

    @Getter
    @Setter
    private RetryExecutor retryExecutor;

    /** No-arg constructor; set provider and optionally retryExecutor before use. */
    public PushChannel() {
    }

    /** Creates a push channel with the given provider; no retry. */
    public PushChannel(NotificationProvider provider) {
        this.provider = provider;
    }

    /** Creates a push channel with the given provider and retry executor. */
    public PushChannel(NotificationProvider provider, RetryExecutor retryExecutor) {
        this.provider = provider;
        this.retryExecutor = retryExecutor;
    }

    /**
     * Sends the notification via the configured provider. If a retry executor is set,
     * uses it; otherwise delegates directly to the provider.
     *
     * @param request the notification request (recipient, title, body/message)
     * @return the result of the send attempt
     * @throws ProviderException if no provider is set or the provider is not configured
     */
    @Override
    public NotificationResult send(NotificationRequest request) {
        if (provider == null) {
            throw new ProviderException("PushChannel", "No push notification provider configured");
        }
        
        if (!provider.isConfigured()) {
            throw new ProviderException("PushChannel", 
                "Push notification provider is not properly configured: " + provider.getName());
        }
        
        log.debug("Sending push notification via provider: {}", provider.getName());
        
        // Use retry executor if configured, otherwise send directly
        if (retryExecutor != null) {
            return retryExecutor.executeWithRetry(provider, request);
        }
        
        return provider.send(request);
    }

    /** Returns {@link Channel#PUSH}. */
    @Override
    public Channel getChannelType() {
        return Channel.PUSH;
    }
}
