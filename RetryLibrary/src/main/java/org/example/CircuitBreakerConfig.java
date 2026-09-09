package org.example;

import java.time.Duration;

public class CircuitBreakerConfig {

    private final int  failureThreshold;
    private final int successCountInHalfOpen;
    private final Duration waitDurationInOpenState;

    public CircuitBreakerConfig(Builder builder) {
        this.failureThreshold = builder.failureThreshold;
        this.successCountInHalfOpen = builder.successCountInHalfOpen;
        this.waitDurationInOpenState = builder.waitDurationInOpenState;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public int getSuccessCountInHalfOpen() {
        return successCountInHalfOpen;
    }

    public Duration getWaitDurationInOpenState() {
        return waitDurationInOpenState;
    }

    public  static  Builder builder() {
        return new Builder();
    }

    public static  CircuitBreakerConfig ofDefaults() {
        return builder().build();
    }

    public static class Builder {

        private  int  failureThreshold = 5;
        private  int successCountInHalfOpen = 2;
        private  Duration waitDurationInOpenState = Duration.ofSeconds(5);


        public Builder failureThreshold(int failureThreshold) {
            this.failureThreshold = failureThreshold;
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

        public CircuitBreakerConfig build() {
            return new CircuitBreakerConfig(this);
        }
    }
}
