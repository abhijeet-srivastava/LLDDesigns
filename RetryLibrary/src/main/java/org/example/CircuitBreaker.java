package org.example;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class CircuitBreaker {

    public enum State {
        CLOSED,
        OPEN,
        HALF_OPEN;
    }

    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);

    private final AtomicLong lastFailureTime = new AtomicLong(0);

    private final AtomicInteger halfOpenCallsAllowed = new AtomicInteger(0);

    private final AtomicReference<State> state = new AtomicReference(State.CLOSED);

    private final CircuitBreakerConfig  config;

    public CircuitBreaker(CircuitBreakerConfig config) {
        this.config = config;
    }

    public CircuitBreaker() {
        this(CircuitBreakerConfig.ofDefaults());
    }

    public <T> T execute(Supplier<T> supplier) {
        checkState();
        try {
            T result = supplier.get();
            onSuccess();
            return result;
        } catch(Exception ex) {
            onFailure(ex);
            throw ex;
        }
    }

    private void onFailure(Exception ex) {
        State current = state.get();
        if(current == State.HALF_OPEN) {
            tripBreaker();
            return;
        }
        int failure = failureCount.incrementAndGet();
        if(failure >= config.getFailureThreshold()) {
            tripBreaker();
        }
    }

    private void tripBreaker() {
        this.state.set(State.OPEN);
        this.successCount.set(0);
        this.failureCount.set(0);
        this.lastFailureTime.set(Instant.now().toEpochMilli());
    }

    private void onSuccess() {
        State current = state.get();
        if(current == State.HALF_OPEN) {
            int successCount = this.successCount.incrementAndGet();
            if(successCount >= config.getSuccessCountInHalfOpen()) {
                reset();
            } else {
                halfOpenCallsAllowed.set(1);
            }
        } else if(current == State.CLOSED) {
            failureCount.set(0)
            ;
        }
    }



    private void reset() {
        this.successCount.set(0);
        this.failureCount.set(0);
        this.state.set(State.CLOSED);
    }

    private void checkState() {
        State current = state.get();
        if(current == State.OPEN) {
            if(shouldReset()) {
                transitionTo(State.HALF_OPEN);
                halfOpenCallsAllowed.set(1);
            } else {
                throw new CircuitBreakerOpenException("Circuit is in open state");
            }
        } else if(current == State.HALF_OPEN) {
            int callAllowed = halfOpenCallsAllowed.getAndDecrement();
            if(callAllowed <= 0) {
                throw new CircuitBreakerOpenException("Circuit is in open state");
            }
        }
    }

    private void transitionTo(State state) {
        this.state.getAndSet(state);
    }

    private boolean shouldReset() {
        return timeUntilReset() <= 0;
    }

    private long timeUntilReset() {
        long timeElapsedFromLastFailure = Instant.now().toEpochMilli() - lastFailureTime.get();
        return Math.max(0, timeElapsedFromLastFailure - config.getWaitDurationInOpenState().toMillis());
    }


}
