package org.example;

import java.util.ArrayDeque;
import java.util.Deque;

public class ClickCounter {
    Deque<Integer> counter;

    private final static  Integer MAX_WINDOW = 300;

    public ClickCounter() {
        this.counter = new ArrayDeque<>();
    }

    public void recordClick(int ts) {
        counter.offerLast(ts);
    }

    public int getRecentClicks(int ts) {
        while(!counter.isEmpty() && counter.peekFirst() <= ts-300) {
            counter.removeFirst();
        }
        return counter.size();
    }
}
