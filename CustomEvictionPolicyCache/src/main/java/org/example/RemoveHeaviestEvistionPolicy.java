package org.example;

import java.util.Comparator;
import java.util.PriorityQueue;

public class RemoveHeaviestEvistionPolicy implements EvictionPolicy {

    PriorityQueue<String> keys;

    public RemoveHeaviestEvistionPolicy() {
        this.keys = new PriorityQueue<>(
                (k1, k2)
                        -> weight(k1) == weight(k2) ? k1.compareTo(k2) : Integer.compare(weight(k2), weight(k1))
        );
    }

    private int weight(String key) {
        int weight = 0;
        for(char ch: key.toCharArray()) {
            weight += (ch - 'a' + 1);
        }
        return weight;
    }

    @Override
    public String nextEvictionKey() {
        return keys.peek();
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
