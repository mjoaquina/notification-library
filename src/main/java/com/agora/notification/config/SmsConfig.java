package com.agora.notification.config;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration for SMS providers.
 * Uses Builder pattern for fluent configuration.
 */
@Getter
@Builder
public class SmsConfig {
    private final String accountSid; 
    private final String authToken;  
    private final String apiKey;     
    private final String apiSecret;  
    private final String region;     
    private final String fromNumber; 
    private final String apiUrl;     
    private final int timeoutMs;     
    
    /**
     * Validates that the configuration is complete for Twilio.
     * 
     * @return true if all required Twilio fields are present
     */
    public boolean isValidForTwilio() {
        return accountSid != null && !accountSid.isBlank()
            && authToken != null && !authToken.isBlank()
            && fromNumber != null && !fromNumber.isBlank()
            && apiUrl != null && !apiUrl.isBlank();
    }
    
    /**
     * Validates that the configuration is complete for AWS SNS.
     * 
     * @return true if all required AWS SNS fields are present
     */
    public boolean isValidForAwsSns() {
        return apiKey != null && !apiKey.isBlank()
            && apiSecret != null && !apiSecret.isBlank()
            && region != null && !region.isBlank()
            && apiUrl != null && !apiUrl.isBlank();
    }
}
