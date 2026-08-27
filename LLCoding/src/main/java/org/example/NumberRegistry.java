package org.example;

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.IntStream;

public class NumberRegistry {
    Set<Integer> allocated;
    PriorityQueue<Integer> pq;
    int count;
    int nextToken;

    public NumberRegistry(int count) {
        this.count = count;
        allocated = new HashSet<>();
        //List<Integer> list = IntStream.range(0, count).boxed().toList();
        //pq = new PriorityQueue<>(list);
        pq = new PriorityQueue<>();
        nextToken = 0;
    }
    public int fetch() {
        if(!pq.isEmpty()) {
            int allocate =  pq.remove();
            allocated.add(allocate);
            return allocate;
        }
        int res = nextToken;
        nextToken += 1;
        return res;
    }
    boolean isAvailable(int num) {
        if(num >= count) {
            return false;
        }
        return !allocated.contains(num);
    }

    void returnNumber(int num) {
        if(num >= count || !allocated.contains(num)) {
            return;
        }
        this.allocated.remove(num);
        pq.offer(num);
    }
}
