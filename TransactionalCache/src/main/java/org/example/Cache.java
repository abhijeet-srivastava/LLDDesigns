package org.example;

public interface Cache {
    Object get(String key);

    void put(String key, Object value);

    void delete(String key);
}