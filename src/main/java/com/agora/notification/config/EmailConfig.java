package com.agora.notification.config;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration for email providers.
 * Uses Builder pattern for fluent configuration.
 */
@Getter
@Builder
public class EmailConfig {
    private final String apiKey;
    private final String fromEmail;
    private final String fromName;
    private final String apiUrl; 
    private final int timeoutMs; 
    
    /**
     * Validates that the configuration is complete.
     * 
     * @return true if all required fields are present
     */
    public boolean isValid() {
        return apiKey != null && !apiKey.isBlank() 
            && fromEmail != null && !fromEmail.isBlank()
            && apiUrl != null && !apiUrl.isBlank();
    }
}
