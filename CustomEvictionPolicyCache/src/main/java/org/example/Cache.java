package org.example;

public interface Cache {
    String get(String key);
    void put(String key, String value);
    String nextEvictionKey();
    boolean remove(String key);
}
