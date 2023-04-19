package com.mqv.monitor.data;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryDatabase {
    private final Map<String, Object> data = new HashMap<>();

    public Object get(String key) {
        return data.get(key);
    }

    public void setKey(String key, Object value) {
        data.put(key, value);
    }
}
