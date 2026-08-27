package org.example;

import java.time.Instant;
import java.util.Arrays;
import java.util.function.Supplier;

public class SlidingWindowCircuitBreaker {

    public enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    private final boolean[] window;
    private int windowIndex = 0;
    private int callsInWindow = 0;
    private int failuresInWindow = 0;

    private int halfOpenSuccesses = 0;
    private int halfOpenCallsAllowed = 0;
    private long lastFailureTime = 0;

    private State state = State.CLOSED;

    private final SlidingWindowCircuitBreakerConfig config;

    public SlidingWindowCircuitBreaker(SlidingWindowCircuitBreakerConfig config) {
        this.config = config;
        this.window = new boolean[config.getWindowSize()];
    }

    public SlidingWindowCircuitBreaker() {
        this(SlidingWindowCircuitBreakerConfig.ofDefaults());
    }

    public synchronized <T> T execute(Supplier<T> supplier) {
        checkState();
        try {
            T result = supplier.get();
            onResult(true);
            return result;
        } catch (Exception ex) {
            onResult(false);
            throw ex;
        }
    }

    public synchronized State getState() {
        return state;
    }

    private void checkState() {
        if (state == State.OPEN) {
            long elapsed = Instant.now().toEpochMilli() - lastFailureTime;
            if (elapsed >= config.getWaitDurationInOpenState().toMillis()) {
                state = State.HALF_OPEN;
                halfOpenCallsAllowed = 1;
                halfOpenSuccesses = 0;
            } else {
                throw new CircuitBreakerOpenException("Circuit is in open state");
            }
        } else if (state == State.HALF_OPEN) {
            if (halfOpenCallsAllowed <= 0) {
                throw new CircuitBreakerOpenException("Circuit is in open state");
            }
            halfOpenCallsAllowed--;
        }
    }

    private void onResult(boolean success) {
        if (state == State.HALF_OPEN) {
            if (success) {
                halfOpenSuccesses++;
                if (halfOpenSuccesses >= config.getSuccessCountInHalfOpen()) {
                    reset();
                } else {
                    halfOpenCallsAllowed = 1;
                }
            } else {
                tripBreaker();
            }
            return;
        }

        recordInWindow(success);
        if (callsInWindow >= config.getMinimumCalls() && failureRate() >= config.getFailureRateThreshold()) {
            tripBreaker();
        }
    }

    private void recordInWindow(boolean success) {
        boolean failed = !success;
        if (callsInWindow < window.length) {
            callsInWindow++;
        } else if (window[windowIndex]) {
            failuresInWindow--;
        }
        window[windowIndex] = failed;
        if (failed) {
            failuresInWindow++;
        }
        windowIndex = (windowIndex + 1) % window.length;
    }

    private double failureRate() {
        return (failuresInWindow * 100.0) / callsInWindow;
    }

    private void tripBreaker() {
        state = State.OPEN;
        lastFailureTime = Instant.now().toEpochMilli();
        clearWindow();
    }

    private void reset() {
        state = State.CLOSED;
        clearWindow();
    }

    private void clearWindow() {
        Arrays.fill(window, false);
        windowIndex = 0;
        callsInWindow = 0;
        failuresInWindow = 0;
    }
}