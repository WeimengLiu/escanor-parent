package com.escanor.rabbitmq.config;

import java.util.List;


public class CustomRouteKeyCache {

    private final List<String> customKeys;

    public CustomRouteKeyCache(List<String> customKeys) {
        this.customKeys = customKeys;
    }

    public boolean existKey(String key) {
        return customKeys.contains(key);
    }
}
