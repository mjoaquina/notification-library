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
 * SMS provider backed by AWS SNS. Simulates delivery; uses message and recipient from the request. 
 */
@Slf4j
@RequiredArgsConstructor
public class AwsSnsProvider implements NotificationProvider {
    
    private static final String PROVIDER_NAME = "AWS SNS";
    private static final int MIN_LATENCY_MS = 100;
    private static final int MAX_LATENCY_MS = 300;
    private static final double SUCCESS_RATE = 0.94; // 94% success rate for simulation
    
    private final SmsConfig config;
    private final Random random = new Random();
    
    @Override
    public NotificationResult send(NotificationRequest request) {
        if (!isConfigured()) {
            throw new ProviderException(PROVIDER_NAME, "Provider is not properly configured");
        }
        
        log.info("[AWS SNS] Preparing SMS send request");
        log.debug("[AWS SNS] Region: {}, To: {}, Message length: {}", 
            config.getRegion(), request.getRecipient(),
            request.getMessage() != null ? request.getMessage().length() : 0);
        
        // Simulate API request structure (as per AWS SNS API docs)
        String apiRequest = buildAwsSnsRequest(request);
        log.debug("[AWS SNS] API Request: {}", apiRequest);
        
        // Simulate network latency
        simulateLatency();
        
        // Simulate API call and response
        boolean success = simulateApiCall();
        
        if (success) {
            log.info("[AWS SNS] SMS sent successfully to {}", request.getRecipient());
            return NotificationResult.builder()
                .success(true)
                .status(NotificationStatus.SENT)
                .message("SMS sent successfully via AWS SNS")
                .providerName(PROVIDER_NAME)
                .timestamp(Instant.now())
                .attemptNumber(1)
                .build();
        } else {
            log.error("[AWS SNS] Failed to send SMS to {}", request.getRecipient());
            throw new ProviderException(
                PROVIDER_NAME,
                "AWS SNS API returned error: 403 Forbidden - Invalid credentials"
            );
        }
    }
    
    private String buildAwsSnsRequest(NotificationRequest request) {
        // Simulates AWS SNS Publish API JSON structure
        return String.format(
            "{\"PhoneNumber\":\"%s\",\"Message\":\"%s\",\"MessageAttributes\":{\"AWS.SNS.SMS.SMSType\":{\"DataType\":\"String\",\"StringValue\":\"Transactional\"}}}",
            request.getRecipient(),
            request.getMessage().replace("\"", "\\\"")
        );
    }
    
    private void simulateLatency() {
        try {
            int latency = MIN_LATENCY_MS + random.nextInt(MAX_LATENCY_MS - MIN_LATENCY_MS);
            TimeUnit.MILLISECONDS.sleep(latency);
            log.debug("[AWS SNS] Simulated latency: {}ms", latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[AWS SNS] Latency simulation interrupted");
        }
    }
    
    private boolean simulateApiCall() {
        // Simulate API validation
        if (config.getApiKey().length() < 15 || config.getApiSecret().length() < 20) {
            log.warn("[AWS SNS] Invalid credentials format (simulated)");
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
        return config != null && config.isValidForAwsSns();
    }
}
