package org.example;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RetryConfig {
    private final int  maxAttempts;
    private final Duration initialDelay;
    private final Duration maxDelay;
    private final  double multiplier;
    private final double jitterFactor;

    private final Set<Class<? extends Throwable>> retryableExceptions;
    private final Set<Class<? extends Throwable>> nonRetryableExceptions;

    private RetryConfig(Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.initialDelay = builder.initialDelay;
        this.maxDelay = builder.maxDelay;
        this.multiplier = builder.multiplier;
        this.jitterFactor = builder.jitterFactor;
        this.retryableExceptions = builder.retryableExceptions;
        this.nonRetryableExceptions = builder.nonRetryableExceptions;
    }

    public boolean isRetryable(Throwable t) {
        for(Class<? extends Throwable> abortOn: nonRetryableExceptions) {
            if(abortOn.isInstance(t)){
                return false;
            }
        }
        for(Class<? extends  Throwable> retryOn: retryableExceptions) {
            if(retryOn.isInstance(t)) {
                return true;
            }
        }
        return false;
    }

    public static Builder builder() {
        return new Builder();
    }



    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration getInitialDelay() {
        return initialDelay;
    }

    public Duration getMaxDelay() {
        return maxDelay;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public double getJitterFactor() {
        return jitterFactor;
    }

    public static RetryConfig ofDefaults() {
        return builder().build();
    }
    public static class Builder {
        private  int  maxAttempts = 3;
        private  Duration initialDelay = Duration.ofMillis(500);
        private  Duration maxDelay = Duration.ofSeconds(30);
        private   double multiplier =2.0;
        private  double jitterFactor = 0.1;

        private  Set<Class<? extends Throwable>> retryableExceptions = new HashSet<>();
        private  Set<Class<? extends Throwable>> nonRetryableExceptions = new HashSet<>();

        public Builder maxAttempts(int maxAttempts) {
            if(maxAttempts < 1) {
                throw new IllegalArgumentException("maxAttempts should be >= 1");
            }
            this.maxAttempts = maxAttempts;
            return this;
        }
        public Builder initialDelay(Duration initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }

        public Builder maxDelay(Duration maxDelay) {
            this.maxDelay = maxDelay;
            return this;
        }

        public Builder multiplier(double multiplier) {
            if(multiplier < 1.0d){
                throw new IllegalArgumentException("multiplier should be >= 1.0");
            }
            this.multiplier = multiplier;
            return this;
        }

        public Builder jitterFactor(double jitterFactor) {
            if(jitterFactor < 0 || jitterFactor > 1){
                throw new IllegalArgumentException("jitterFactor should be > 0 and jitterFactor < 1");
            }
            this.jitterFactor = jitterFactor;
            return this;
        }

        @SafeVarargs
        public final Builder retryOn(Class<? extends Throwable>... exceptions){
            this.retryableExceptions.addAll(Arrays.asList(exceptions));
            return this;
        }

        @SafeVarargs
        public final Builder abortOn(Class<? extends Throwable>... exceptions){
            this.nonRetryableExceptions.addAll(Arrays.asList(exceptions));
            return this;
        }

        public RetryConfig build() {
            return new RetryConfig(this);
        }

    }
}
