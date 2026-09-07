package org.example;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Supplier;

public class MaxFrequenciesInWindows {
    Map<Integer, Integer> counter;
    int size;
    Supplier<Integer> generator;
    Deque<Integer> queue;
    public MaxFrequenciesInWindows(int size, Supplier<Integer> generator) {
        this.size = size;
        this.generator = generator;
        this.counter = new HashMap<>();
        this.queue = new ArrayDeque<>();
    }

    public void generate() {
        int num = generator.get();
        //System.out.println("Generated : " + num);
        counter.merge(num, 1, Integer::sum);
        queue.offerFirst(num);
        if(queue.size() > size) {
            int rem = queue.removeLast();
            counter.merge(rem, -1, Integer::sum);
            if(counter.get(rem) == 0) {
                counter.remove(rem);
            }
        }
    }
    public List<int[]> topK(int k) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        for(var entry: counter.entrySet()) {
            pq.offer(new int[]{entry.getKey(), entry.getValue()});
            if(pq.size() > k) {
                pq.remove();
            }
        }
        return pq.stream().toList();
    }
}
