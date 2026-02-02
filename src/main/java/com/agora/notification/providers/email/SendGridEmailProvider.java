package com.agora.notification.providers.email;

import com.agora.notification.config.EmailConfig;
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
 * Email provider backed by SendGrid. Simulates delivery; uses subject and message from the request. 
 */
@Slf4j
@RequiredArgsConstructor
public class SendGridEmailProvider implements NotificationProvider {
    
    private static final String PROVIDER_NAME = "SendGrid";
    private static final int MIN_LATENCY_MS = 50;
    private static final int MAX_LATENCY_MS = 200;
    private static final double SUCCESS_RATE = 0.95; // 95% success rate for simulation
    
    private final EmailConfig config;
    private final Random random = new Random();
    
    @Override
    public NotificationResult send(NotificationRequest request) {
        if (!isConfigured()) {
            throw new ProviderException(PROVIDER_NAME, "Provider is not properly configured");
        }
        
        log.info("[SendGrid] Preparing email send request");
        log.debug("[SendGrid] From: {}, To: {}, Subject: {}", 
            config.getFromEmail(), request.getRecipient(), request.getSubject());
        
        // Simulate API request structure (as per SendGrid API docs)
        String apiRequest = buildSendGridRequest(request);
        log.debug("[SendGrid] API Request: {}", apiRequest);
        
        // Simulate network latency
        simulateLatency();
        
        // Simulate API call and response
        boolean success = simulateApiCall();
        
        if (success) {
            log.info("[SendGrid] Email sent successfully to {}", request.getRecipient());
            return NotificationResult.builder()
                .success(true)
                .status(NotificationStatus.SENT)
                .message("Email sent successfully via SendGrid")
                .providerName(PROVIDER_NAME)
                .timestamp(Instant.now())
                .attemptNumber(1)
                .build();
        } else {
            log.error("[SendGrid] Failed to send email to {}", request.getRecipient());
            throw new ProviderException(
                PROVIDER_NAME,
                "SendGrid API returned error: 400 Bad Request - Invalid email address"
            );
        }
    }
    
    private String buildSendGridRequest(NotificationRequest request) {
        // Simulates SendGrid API v3 Mail Send JSON structure
        return String.format(
            "{\"personalizations\":[{\"to\":[{\"email\":\"%s\"}]}],\"from\":{\"email\":\"%s\",\"name\":\"%s\"},\"subject\":\"%s\",\"content\":[{\"type\":\"text/plain\",\"value\":\"%s\"}]}",
            request.getRecipient(),
            config.getFromEmail(),
            config.getFromName() != null ? config.getFromName() : "",
            request.getSubject(),
            request.getMessage().replace("\"", "\\\"")
        );
    }
    
    private void simulateLatency() {
        try {
            int latency = MIN_LATENCY_MS + random.nextInt(MAX_LATENCY_MS - MIN_LATENCY_MS);
            TimeUnit.MILLISECONDS.sleep(latency);
            log.debug("[SendGrid] Simulated latency: {}ms", latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[SendGrid] Latency simulation interrupted");
        }
    }
    
    private boolean simulateApiCall() {
        // Simulate API validation
        if (config.getApiKey().length() < 20) {
            log.warn("[SendGrid] Invalid API key format (simulated)");
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
        return config != null && config.isValid();
    }
}
