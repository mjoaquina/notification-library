package com.agora.notification.providers.push;

import com.agora.notification.config.PushConfig;
import com.agora.notification.core.NotificationProvider;
import com.agora.notification.exceptions.ProviderException;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.models.NotificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/** 
 * Push provider backed by OneSignal. Simulates delivery; uses title and body (recipient = device token). 
 */
@Slf4j
@RequiredArgsConstructor
public class OneSignalProvider implements NotificationProvider {
    
    private static final String PROVIDER_NAME = "OneSignal";
    private static final int MIN_LATENCY_MS = 60;
    private static final int MAX_LATENCY_MS = 200;
    private static final double SUCCESS_RATE = 0.93; // 93% success rate for simulation
    
    private final PushConfig config;
    private final Random random = new Random();
    
    @Override
    public NotificationResult send(NotificationRequest request) {
        if (!isConfigured()) {
            throw new ProviderException(PROVIDER_NAME, "Provider is not properly configured");
        }
        
        log.info("[OneSignal] Preparing push notification send request");
        log.debug("[OneSignal] App ID: {}, To: {}, Title: {}, Body: {}", 
            config.getAppId(), request.getRecipient(), request.getTitle(), request.getBody());
        
        // Simulate API request structure (as per OneSignal API docs)
        String apiRequest = buildOneSignalRequest(request);
        log.debug("[OneSignal] API Request: {}", apiRequest);
        
        // Simulate network latency
        simulateLatency();
        
        // Simulate API call and response
        boolean success = simulateApiCall();
        
        if (success) {
            log.info("[OneSignal] Push notification sent successfully to {}", request.getRecipient());
            return NotificationResult.builder()
                .success(true)
                .status(NotificationStatus.SENT)
                .message("Push notification sent successfully via OneSignal")
                .providerName(PROVIDER_NAME)
                .timestamp(Instant.now())
                .attemptNumber(1)
                .build();
        } else {
            log.error("[OneSignal] Failed to send push notification to {}", request.getRecipient());
            throw new ProviderException(
                PROVIDER_NAME,
                "OneSignal API returned error: 400 Bad Request - Invalid app ID or API key"
            );
        }
    }
    
    private String buildOneSignalRequest(NotificationRequest request) {
        // Simulates OneSignal Create Notification API JSON structure
        String title = request.getTitle() != null ? request.getTitle() : "";
        String body = request.getBody() != null ? request.getBody() : request.getMessage();
        
        return String.format(
            "{\"app_id\":\"%s\",\"include_player_ids\":[\"%s\"],\"headings\":{\"en\":\"%s\"},\"contents\":{\"en\":\"%s\"}}",
            config.getAppId(),
            request.getRecipient(),
            title.replace("\"", "\\\""),
            body != null ? body.replace("\"", "\\\"") : ""
        );
    }
    
    private void simulateLatency() {
        try {
            int latency = MIN_LATENCY_MS + random.nextInt(MAX_LATENCY_MS - MIN_LATENCY_MS);
            TimeUnit.MILLISECONDS.sleep(latency);
            log.debug("[OneSignal] Simulated latency: {}ms", latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[OneSignal] Latency simulation interrupted");
        }
    }
    
    private boolean simulateApiCall() {
        // Simulate API validation
        if (config.getApiKey().length() < 20 || config.getAppId().length() < 20) {
            log.warn("[OneSignal] Invalid credentials format (simulated)");
            return false;
        }
        
        // Simulate random failures for testing
        return random.nextDouble() < SUCCESS_RATE;
    }
    
    @Override
    public String getName() {
        return PROVIDER_NAME;
    }
    
    @Override
    public boolean isConfigured() {
        return config != null && config.isValidForOneSignal();
    }
}
