package org.example;

import java.util.HashMap;
import java.util.Map;

public class CustomEvictionCache implements Cache {

    private final int capacity;
    private final EvictionPolicy evictionPolicy;
    private final Map<String, String> cache;

    public CustomEvictionCache(int capacity, EvictionPolicy evictionPolicy) {
        this.capacity = capacity;
        this.evictionPolicy = evictionPolicy;
        this.cache = new HashMap<>();
    }

    @Override
    public String get(String key) {
        if(!this.cache.containsKey(key)) {
            return "";
        }
        this.evictionPolicy.touchKey(key);
        return cache.get(key);
    }

    @Override
    public void put(String key, String value) {
        if(!this.cache.containsKey(key)) {
            this.evictionPolicy.addKey(key);
        } else {
            this.evictionPolicy.touchKey(key);
        }
        this.cache.put(key, value);
        if(this.cache.size() > this.capacity) {
            String keyToEvict = this.nextEvictionKey();
            cache.remove(keyToEvict);
            evictionPolicy.removeKey(keyToEvict);
        }
    }

    @Override
    public String nextEvictionKey() {
        return this.evictionPolicy.nextEvictionKey();
    }

    @Override
    public boolean remove(String key) {
        if(this.cache.containsKey(key)) {
            this.cache.remove(key);
            this.evictionPolicy.removeKey(key);
            return true;
        };
        return false;
    }
}
