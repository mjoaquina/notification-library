package com.agora.notification.providers.sms;

import com.agora.notification.config.SmsConfig;
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
 * SMS provider backed by Twilio. Simulates delivery; uses message and recipient from the request. 
 */
@Slf4j
@RequiredArgsConstructor
public class TwilioSmsProvider implements NotificationProvider {
    
    private static final String PROVIDER_NAME = "Twilio";
    private static final int MIN_LATENCY_MS = 80;
    private static final int MAX_LATENCY_MS = 250;
    private static final double SUCCESS_RATE = 0.96; // 96% success rate for simulation
    
    private final SmsConfig config;
    private final Random random = new Random();
    
    @Override
    public NotificationResult send(NotificationRequest request) {
        if (!isConfigured()) {
            throw new ProviderException(PROVIDER_NAME, "Provider is not properly configured");
        }
        
        log.info("[Twilio] Preparing SMS send request");
        log.debug("[Twilio] From: {}, To: {}, Message length: {}", 
            config.getFromNumber(), request.getRecipient(), 
            request.getMessage() != null ? request.getMessage().length() : 0);
        
        // Simulate API request structure (as per Twilio API docs)
        String apiRequest = buildTwilioRequest(request);
        log.debug("[Twilio] API Request: {}", apiRequest);
        
        // Simulate network latency
        simulateLatency();
        
        // Simulate API call and response
        boolean success = simulateApiCall();
        
        if (success) {
            log.info("[Twilio] SMS sent successfully to {}", request.getRecipient());
            return NotificationResult.builder()
                .success(true)
                .status(NotificationStatus.SENT)
                .message("SMS sent successfully via Twilio")
                .providerName(PROVIDER_NAME)
                .timestamp(Instant.now())
                .attemptNumber(1)
                .build();
        } else {
            log.error("[Twilio] Failed to send SMS to {}", request.getRecipient());
            throw new ProviderException(
                PROVIDER_NAME,
                "Twilio API returned error: 400 Bad Request - Invalid phone number"
            );
        }
    }
    
    private String buildTwilioRequest(NotificationRequest request) {
        // Simulates Twilio API Messages endpoint form-data structure
        return String.format(
            "From=%s&To=%s&Body=%s",
            config.getFromNumber(),
            request.getRecipient(),
            request.getMessage()
        );
    }
    
    private void simulateLatency() {
        try {
            int latency = MIN_LATENCY_MS + random.nextInt(MAX_LATENCY_MS - MIN_LATENCY_MS);
            TimeUnit.MILLISECONDS.sleep(latency);
            log.debug("[Twilio] Simulated latency: {}ms", latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[Twilio] Latency simulation interrupted");
        }
    }
    
    private boolean simulateApiCall() {
        // Simulate API validation
        if (config.getAccountSid().length() < 20 || config.getAuthToken().length() < 20) {
            log.warn("[Twilio] Invalid credentials format (simulated)");
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
        return config != null && config.isValidForTwilio();
    }
}
