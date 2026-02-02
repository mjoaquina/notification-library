package com.agora.notification.service;

import com.agora.notification.channels.EmailChannel;
import com.agora.notification.core.NotificationChannel;
import com.agora.notification.models.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationChannelRegistryTest {
    
    @Mock
    private NotificationChannel mockChannel;
    
    private NotificationChannelRegistry registry;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registry = new NotificationChannelRegistry();
    }
    
    @Test
    void testRegister() {
        when(mockChannel.getChannelType()).thenReturn(Channel.EMAIL);
        
        registry.register(mockChannel);
        
        NotificationChannel retrieved = registry.getChannel(Channel.EMAIL);
        assertEquals(mockChannel, retrieved);
    }
    
    @Test
    void testRegisterNull() {
        registry.register(null);
        
        assertNull(registry.getChannel(Channel.EMAIL));
    }
    
    @Test
    void testGetChannel() {
        when(mockChannel.getChannelType()).thenReturn(Channel.SMS);
        
        registry.register(mockChannel);
        
        NotificationChannel retrieved = registry.getChannel(Channel.SMS);
        assertEquals(mockChannel, retrieved);
        
        // Non-registered channel should return null
        assertNull(registry.getChannel(Channel.PUSH));
    }
    
    @Test
    void testUnregister() {
        when(mockChannel.getChannelType()).thenReturn(Channel.EMAIL);
        
        registry.register(mockChannel);
        assertNotNull(registry.getChannel(Channel.EMAIL));
        
        registry.unregister(Channel.EMAIL);
        assertNull(registry.getChannel(Channel.EMAIL));
    }
    
    @Test
    void testClear() {
        when(mockChannel.getChannelType()).thenReturn(Channel.EMAIL);
        
        registry.register(mockChannel);
        assertNotNull(registry.getChannel(Channel.EMAIL));
        
        registry.clear();
        assertNull(registry.getChannel(Channel.EMAIL));
    }
    
    @Test
    void testRegisterMultipleChannels() {
        EmailChannel emailChannel = new EmailChannel();
        NotificationChannel smsChannel = mock(NotificationChannel.class);
        when(smsChannel.getChannelType()).thenReturn(Channel.SMS);

        registry.register(emailChannel);
        registry.register(smsChannel);

        assertEquals(emailChannel, registry.getChannel(Channel.EMAIL));
        assertEquals(smsChannel, registry.getChannel(Channel.SMS));
    }

    @Test
    void testGetChannels_returnsRegisteredChannelsMap() {
        when(mockChannel.getChannelType()).thenReturn(Channel.EMAIL);
        registry.register(mockChannel);

        assertNotNull(registry.getChannels());
        assertEquals(1, registry.getChannels().size());
        assertEquals(mockChannel, registry.getChannels().get(Channel.EMAIL));
    }
}
