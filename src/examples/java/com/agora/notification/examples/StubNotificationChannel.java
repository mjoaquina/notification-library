package com.agora.notification.examples;

import com.agora.notification.core.NotificationChannel;
import com.agora.notification.core.NotificationProvider;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.models.NotificationStatus;

import java.time.Instant;

/**
 * Stub channel that always returns simulated success.
 * Used in examples to demonstrate the success path without real provider calls,
 * as allowed by the requirements (simulate sending, return simulated results).
 */
public final class StubNotificationChannel implements NotificationChannel {

    private static final String STUB_PROVIDER_NAME = "Stub (simulated)";

    private final Channel channelType;

    public StubNotificationChannel(Channel channelType) {
        this.channelType = channelType;
    }

    @Override
    public NotificationResult send(NotificationRequest request) {
        return NotificationResult.builder()
                .success(true)
                .status(NotificationStatus.SENT)
                .message("Simulated success via stub channel")
                .providerName(STUB_PROVIDER_NAME)
                .timestamp(Instant.now())
                .attemptNumber(1)
                .build();
    }

    @Override
    public Channel getChannelType() {
        return channelType;
    }

    @Override
    public void setProvider(NotificationProvider provider) {
        // no-op for stub
    }

    @Override
    public NotificationProvider getProvider() {
        return null;
    }
}
