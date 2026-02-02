package com.agora.notification.factory;

import com.agora.notification.core.NotificationChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds channel factories by key "CHANNEL_TYPE:PROVIDER_NAME" (e.g. "EMAIL:SendGrid").
 * Thread-safe. Replacing a factory for the same key overwrites the previous one; null is ignored.
 */
@Slf4j
public class ChannelFactoryRegistry implements ChannelFactoryRegistryInterface {

    private static final ChannelFactoryRegistry instance = new ChannelFactoryRegistry();

    private final Map<String, ChannelFactoryInterface> factories = new ConcurrentHashMap<>();

    private ChannelFactoryRegistry() {}

    public static ChannelFactoryRegistry getInstance() {
        return instance;
    }
    
    public void register(ChannelFactoryInterface factory) {
        if (factory != null) {
            String key = createKey(factory.getChannelType(), factory.getProviderName());
            factories.put(key, factory);
            log.debug("Registered channel factory: {}:{}", 
                factory.getChannelType(), factory.getProviderName());
        }
    }
    
    public NotificationChannel createChannel(String channelType, String providerName, String... config) {
        String key = createKey(channelType, providerName);
        ChannelFactoryInterface factory = factories.get(key);
        
        if (factory != null) {
            return factory.createChannel(config);
        }
        
        log.warn("No factory registered for {}:{}", channelType, providerName);
        return null;
    }
    
    public boolean isRegistered(String channelType, String providerName) {
        String key = createKey(channelType, providerName);
        return factories.containsKey(key);
    }
    
    public void unregister(String channelType, String providerName) {
        String key = createKey(channelType, providerName);
        factories.remove(key);
        log.debug("Unregistered channel factory: {}:{}", channelType, providerName);
    }
    
    public void clear() {
        factories.clear();
        log.debug("Cleared all channel factories");
    }
    
    private String createKey(String channelType, String providerName) {
        return channelType.toUpperCase() + ":" + providerName;
    }
}
