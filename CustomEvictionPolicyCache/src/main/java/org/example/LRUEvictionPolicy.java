package org.example;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUEvictionPolicy implements EvictionPolicy {

    private final LinkedHashMap<String, Boolean> keys = new LinkedHashMap<>(16, 0.75f, true);

    @Override
    public String nextEvictionKey() {
        Map.Entry<String, Boolean> eldest = keys.entrySet().stream().findFirst().orElse(null);
        return eldest == null ? "" : eldest.getKey();
    }

    @Override
    public void addKey(String key) {
        keys.put(key, Boolean.TRUE);
    }

    @Override
    public void removeKey(String key) {
        keys.remove(key);
    }

    @Override
    public void touchKey(String key) {
        keys.get(key);
    }
}