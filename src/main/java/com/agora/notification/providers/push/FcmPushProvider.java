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
 * Push provider backed by Firebase Cloud Messaging. Simulates delivery; 
 * uses title and body (recipient = device token). 
 */
@Slf4j
@RequiredArgsConstructor
public class FcmPushProvider implements NotificationProvider {
    
    private static final String PROVIDER_NAME = "FCM";
    private static final int MIN_LATENCY_MS = 40;
    private static final int MAX_LATENCY_MS = 150;
    private static final double SUCCESS_RATE = 0.97; // 97% success rate for simulation
    
    private final PushConfig config;
    private final Random random = new Random();
    
    @Override
    public NotificationResult send(NotificationRequest request) {
        if (!isConfigured()) {
            throw new ProviderException(PROVIDER_NAME, "Provider is not properly configured");
        }
        
        log.info("[FCM] Preparing push notification send request");
        log.debug("[FCM] To: {}, Title: {}, Body: {}", 
            request.getRecipient(), request.getTitle(), request.getBody());
        
        // Simulate API request structure (as per FCM API docs)
        String apiRequest = buildFcmRequest(request);
        log.debug("[FCM] API Request: {}", apiRequest);
        
        // Simulate network latency
        simulateLatency();
        
        // Simulate API call and response
        boolean success = simulateApiCall();
        
        if (success) {
            log.info("[FCM] Push notification sent successfully to {}", request.getRecipient());
            return NotificationResult.builder()
                .success(true)
                .status(NotificationStatus.SENT)
                .message("Push notification sent successfully via FCM")
                .providerName(PROVIDER_NAME)
                .timestamp(Instant.now())
                .attemptNumber(1)
                .build();
        } else {
            log.error("[FCM] Failed to send push notification to {}", request.getRecipient());
            throw new ProviderException(
                PROVIDER_NAME,
                "FCM API returned error: 401 Unauthorized - Invalid server key"
            );
        }
    }
    
    private String buildFcmRequest(NotificationRequest request) {
        // Simulates FCM v1 Send API JSON structure
        String title = request.getTitle() != null ? request.getTitle() : "";
        String body = request.getBody() != null ? request.getBody() : request.getMessage();
        
        return String.format(
            "{\"message\":{\"token\":\"%s\",\"notification\":{\"title\":\"%s\",\"body\":\"%s\"}}}",
            request.getRecipient(),
            title.replace("\"", "\\\""),
            body != null ? body.replace("\"", "\\\"") : ""
        );
    }
    
    private void simulateLatency() {
        try {
            int latency = MIN_LATENCY_MS + random.nextInt(MAX_LATENCY_MS - MIN_LATENCY_MS);
            TimeUnit.MILLISECONDS.sleep(latency);
            log.debug("[FCM] Simulated latency: {}ms", latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[FCM] Latency simulation interrupted");
        }
    }
    
    private boolean simulateApiCall() {
        // Simulate API validation
        if (config.getServerKey().length() < 50) {
            log.warn("[FCM] Invalid server key format (simulated)");
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
        return config != null && config.isValidForFcm();
    }
}
