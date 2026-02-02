package com.agora.notification.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailConfigTest {
    
    @Test
    void testValidConfig() {
        EmailConfig config = EmailConfig.builder()
            .apiKey("test-api-key-12345678901234567890")
            .fromEmail("sender@example.com")
            .fromName("Test Sender")
            .apiUrl("https://api.example.com")
            .timeoutMs(5000)
            .build();
        
        assertTrue(config.isValid());
        assertEquals("test-api-key-12345678901234567890", config.getApiKey());
        assertEquals("sender@example.com", config.getFromEmail());
        assertEquals("Test Sender", config.getFromName());
        assertEquals("https://api.example.com", config.getApiUrl());
        assertEquals(5000, config.getTimeoutMs());
    }
    
    @Test
    void testInvalidConfigWithNullApiKey() {
        EmailConfig config = EmailConfig.builder()
            .apiKey(null)
            .fromEmail("sender@example.com")
            .apiUrl("https://api.example.com")
            .build();
        
        assertFalse(config.isValid());
    }
    
    @Test
    void testInvalidConfigWithBlankApiKey() {
        EmailConfig config = EmailConfig.builder()
            .apiKey("   ")
            .fromEmail("sender@example.com")
            .apiUrl("https://api.example.com")
            .build();
        
        assertFalse(config.isValid());
    }
    
    @Test
    void testInvalidConfigWithNullFromEmail() {
        EmailConfig config = EmailConfig.builder()
            .apiKey("test-api-key")
            .fromEmail(null)
            .apiUrl("https://api.example.com")
            .build();
        
        assertFalse(config.isValid());
    }
    
    @Test
    void testInvalidConfigWithBlankFromEmail() {
        EmailConfig config = EmailConfig.builder()
            .apiKey("test-api-key")
            .fromEmail("   ")
            .apiUrl("https://api.example.com")
            .build();
        
        assertFalse(config.isValid());
    }
    
    @Test
    void testInvalidConfigWithNullApiUrl() {
        EmailConfig config = EmailConfig.builder()
            .apiKey("test-api-key")
            .fromEmail("sender@example.com")
            .apiUrl(null)
            .build();
        
        assertFalse(config.isValid());
    }
    
    @Test
    void testInvalidConfigWithBlankApiUrl() {
        EmailConfig config = EmailConfig.builder()
            .apiKey("test-api-key")
            .fromEmail("sender@example.com")
            .apiUrl("   ")
            .build();
        
        assertFalse(config.isValid());
    }
    
    @Test
    void testConfigWithOptionalFields() {
        EmailConfig config = EmailConfig.builder()
            .apiKey("test-api-key")
            .fromEmail("sender@example.com")
            .apiUrl("https://api.example.com")
            .fromName(null) // Optional
            .build();
        
        assertTrue(config.isValid());
        assertNull(config.getFromName());
    }
    
    @Test
    void testConfigBuilderFluentInterface() {
        EmailConfig config = EmailConfig.builder()
            .apiKey("key")
            .fromEmail("email")
            .apiUrl("url")
            .fromName("name")
            .timeoutMs(1000)
            .build();
        
        assertNotNull(config);
        assertEquals("key", config.getApiKey());
        assertEquals("email", config.getFromEmail());
        assertEquals("url", config.getApiUrl());
        assertEquals("name", config.getFromName());
        assertEquals(1000, config.getTimeoutMs());
    }
}
