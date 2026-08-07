package org.example;

import java.util.Comparator;
import java.util.PriorityQueue;

public class RemoveLargestEvictionPolicy implements EvictionPolicy {
    PriorityQueue<String> keys;

    public RemoveLargestEvictionPolicy() {
        this.keys = new PriorityQueue<>(
                (a, b) -> a.length() == b.length() ? a.compareTo(b) : Integer.compare(b.length(), a.length())
        );
    }

    @Override
    public String nextEvictionKey() {
        return this.keys.isEmpty() ? "" : this.keys.peek();
    }

    @Override
    public void addKey(String key) {
        this.keys.offer(key);
    }

    @Override
    public void removeKey(String key) {
        this.keys.remove(key);
    }
}
