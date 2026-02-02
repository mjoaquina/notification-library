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
 * Email provider backed by Mailgun. Simulates delivery; uses subject and message from the request. 
 */
@Slf4j
@RequiredArgsConstructor
public class MailgunEmailProvider implements NotificationProvider {
    
    private static final String PROVIDER_NAME = "Mailgun";
    private static final int MIN_LATENCY_MS = 60;
    private static final int MAX_LATENCY_MS = 180;
    private static final double SUCCESS_RATE = 0.92; // 92% success rate for simulation
    
    private final EmailConfig config;
    private final Random random = new Random();
    
    @Override
    public NotificationResult send(NotificationRequest request) {
        if (!isConfigured()) {
            throw new ProviderException(PROVIDER_NAME, "Provider is not properly configured");
        }
        
        log.info("[Mailgun] Preparing email send request");
        log.debug("[Mailgun] From: {}, To: {}, Subject: {}", 
            config.getFromEmail(), request.getRecipient(), request.getSubject());
        
        // Simulate API request structure (as per Mailgun API docs)
        String apiRequest = buildMailgunRequest(request);
        log.debug("[Mailgun] API Request: {}", apiRequest);
        
        // Simulate network latency
        simulateLatency();
        
        // Simulate API call and response
        boolean success = simulateApiCall();
        
        if (success) {
            log.info("[Mailgun] Email sent successfully to {}", request.getRecipient());
            return NotificationResult.builder()
                .success(true)
                .status(NotificationStatus.SENT)
                .message("Email sent successfully via Mailgun")
                .providerName(PROVIDER_NAME)
                .timestamp(Instant.now())
                .attemptNumber(1)
                .build();
        } else {
            log.error("[Mailgun] Failed to send email to {}", request.getRecipient());
            throw new ProviderException(
                PROVIDER_NAME,
                "Mailgun API returned error: 401 Unauthorized - Invalid API key"
            );
        }
    }
    
    private String buildMailgunRequest(NotificationRequest request) {
        // Simulates Mailgun API Messages endpoint form-data structure
        String from = config.getFromName() != null && !config.getFromName().isBlank()
            ? String.format("%s <%s>", config.getFromName(), config.getFromEmail())
            : config.getFromEmail();
        
        return String.format(
            "from=%s&to=%s&subject=%s&text=%s",
            from,
            request.getRecipient(),
            request.getSubject(),
            request.getMessage()
        );
    }
    
    private void simulateLatency() {
        try {
            int latency = MIN_LATENCY_MS + random.nextInt(MAX_LATENCY_MS - MIN_LATENCY_MS);
            TimeUnit.MILLISECONDS.sleep(latency);
            log.debug("[Mailgun] Simulated latency: {}ms", latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[Mailgun] Latency simulation interrupted");
        }
    }
    
    private boolean simulateApiCall() {
        // Simulate API validation
        if (config.getApiKey().length() < 20) {
            log.warn("[Mailgun] Invalid API key format (simulated)");
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
