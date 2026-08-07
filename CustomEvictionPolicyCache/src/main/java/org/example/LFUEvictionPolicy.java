package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class LFUEvictionPolicy implements EvictionPolicy {

    private final Map<String, Integer> frequencies = new HashMap<>();
    private final Map<String, Long> insertionOrder = new HashMap<>();
    private long sequence = 0;
    private final PriorityQueue<String> keys = new PriorityQueue<>(
            (k1, k2) -> {
                int freqCompare = Integer.compare(frequencies.get(k1), frequencies.get(k2));
                return freqCompare != 0 ? freqCompare : Long.compare(insertionOrder.get(k1), insertionOrder.get(k2));
            }
    );

    @Override
    public String nextEvictionKey() {
        return keys.isEmpty() ? "" : keys.peek();
    }

    @Override
    public void addKey(String key) {
        frequencies.put(key, 1);
        insertionOrder.put(key, sequence++);
        keys.offer(key);
    }

    @Override
    public void removeKey(String key) {
        keys.remove(key);
        frequencies.remove(key);
        insertionOrder.remove(key);
    }

    @Override
    public void touchKey(String key) {
        keys.remove(key);
        frequencies.merge(key, 1, Integer::sum);
        keys.offer(key);
    }
}