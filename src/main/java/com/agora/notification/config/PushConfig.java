package com.agora.notification.config;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration for push notification providers.
 * Uses Builder pattern for fluent configuration.
 */
@Getter
@Builder
public class PushConfig {
    private final String serverKey;  
    private final String apiKey;  
    private final String appId;       
    private final String apiUrl;       
    private final int timeoutMs;       
    
    /**
     * Validates that the configuration is complete for FCM.
     * 
     * @return true if all required FCM fields are present
     */
    public boolean isValidForFcm() {
        return serverKey != null && !serverKey.isBlank()
            && apiUrl != null && !apiUrl.isBlank();
    }
    
    /**
     * Validates that the configuration is complete for OneSignal.
     * 
     * @return true if all required OneSignal fields are present
     */
    public boolean isValidForOneSignal() {
        return apiKey != null && !apiKey.isBlank()
            && appId != null && !appId.isBlank()
            && apiUrl != null && !apiUrl.isBlank();
    }
}
