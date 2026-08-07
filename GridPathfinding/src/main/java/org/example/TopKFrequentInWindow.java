package org.example;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class TopKFrequentInWindow {

    private final int windowSize;
    private final int k;
    private final Deque<Integer> window = new ArrayDeque<>();
    private final Map<Integer, Integer> freq = new HashMap<>();
    private final TreeMap<Integer, Set<Integer>> freqToElems = new TreeMap<>();

    public TopKFrequentInWindow(int windowSize, int k) {
        this.windowSize = windowSize;
        this.k = k;
    }

    public void add(int value) {
        window.addLast(value);
        bump(value, 1);
        if (window.size() > windowSize) {
            int evicted = window.pollFirst();
            bump(evicted, -1);
        }
    }

    public List<Integer> topK() {
        List<Integer> result = new ArrayList<>();
        for (Set<Integer> elems : freqToElems.descendingMap().values()) {
            for (int elem : elems) {
                result.add(elem);
                if (result.size() == k) {
                    return result;
                }
            }
        }
        return result;
    }

    private void bump(int value, int delta) {
        int oldFreq = freq.getOrDefault(value, 0);
        int newFreq = oldFreq + delta;

        if (oldFreq > 0) {
            Set<Integer> elems = freqToElems.get(oldFreq);
            elems.remove(value);
            if (elems.isEmpty()) {
                freqToElems.remove(oldFreq);
            }
        }

        if (newFreq > 0) {
            freq.put(value, newFreq);
            freqToElems.computeIfAbsent(newFreq, f -> new LinkedHashSet<>()).add(value);
        } else {
            freq.remove(value);
        }
    }
}