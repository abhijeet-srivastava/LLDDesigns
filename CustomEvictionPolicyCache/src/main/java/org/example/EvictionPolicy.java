package org.example;

public interface EvictionPolicy {
    String nextEvictionKey();

    void addKey(String key);

    void removeKey(String key);

    default void touchKey(String key) {
    }
}
