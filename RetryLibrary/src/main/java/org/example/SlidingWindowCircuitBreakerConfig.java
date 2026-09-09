package org.example;

import java.time.Duration;

public class SlidingWindowCircuitBreakerConfig {

    private final int windowSize;
    private final int minimumCalls;
    private final double failureRateThreshold;
    private final int successCountInHalfOpen;
    private final Duration waitDurationInOpenState;

    private SlidingWindowCircuitBreakerConfig(Builder builder) {
        this.windowSize = builder.windowSize;
        this.minimumCalls = builder.minimumCalls;
        this.failureRateThreshold = builder.failureRateThreshold;
        this.successCountInHalfOpen = builder.successCountInHalfOpen;
        this.waitDurationInOpenState = builder.waitDurationInOpenState;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getMinimumCalls() {
        return minimumCalls;
    }

    public double getFailureRateThreshold() {
        return failureRateThreshold;
    }

    public int getSuccessCountInHalfOpen() {
        return successCountInHalfOpen;
    }

    public Duration getWaitDurationInOpenState() {
        return waitDurationInOpenState;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SlidingWindowCircuitBreakerConfig ofDefaults() {
        return builder().build();
    }

    public static class Builder {
        private int windowSize = 10;
        private int minimumCalls = 10;
        private double failureRateThreshold = 50.0;
        private int successCountInHalfOpen = 2;
        private Duration waitDurationInOpenState = Duration.ofSeconds(5);

        public Builder windowSize(int windowSize) {
            if (windowSize < 1) {
                throw new IllegalArgumentException("windowSize should be >= 1");
            }
            this.windowSize = windowSize;
            return this;
        }

        public Builder minimumCalls(int minimumCalls) {
            if (minimumCalls < 1) {
                throw new IllegalArgumentException("minimumCalls should be >= 1");
            }
            this.minimumCalls = minimumCalls;
            return this;
        }

        public Builder failureRateThreshold(double failureRateThreshold) {
            if (failureRateThreshold <= 0 || failureRateThreshold > 100) {
                throw new IllegalArgumentException("failureRateThreshold should be > 0 and <= 100");
            }
            this.failureRateThreshold = failureRateThreshold;
            return this;
        }

        public Builder successCountInHalfOpen(int successCountInHalfOpen) {
            this.successCountInHalfOpen = successCountInHalfOpen;
            return this;
        }

        public Builder waitDurationInOpenState(Duration waitDurationInOpenState) {
            this.waitDurationInOpenState = waitDurationInOpenState;
            return this;
        }

        public SlidingWindowCircuitBreakerConfig build() {
            if (minimumCalls > windowSize) {
                throw new IllegalArgumentException("minimumCalls cannot exceed windowSize");
            }
            return new SlidingWindowCircuitBreakerConfig(this);
        }
    }
}