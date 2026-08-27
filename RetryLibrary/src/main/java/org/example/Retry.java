package org.example;

import java.time.Duration;
import java.util.Random;
import java.util.function.Function;

public class Retry {

    private final RetryConfig retryConfig;
    private final Random random;

    public Retry(RetryConfig retryConfig) {
        this.retryConfig = retryConfig;
        this.random = new Random();
    }

    public <T,R> RetryResult<R> executeWithRetry(Function<T, R> function, T args) {
        Throwable lastException = null;
        for(int attempt = 1; attempt <= retryConfig.getMaxAttempts(); attempt++) {
            Duration delay = calculateDelay(attempt);
            if(!delay.isZero()) {
                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
            try {
                return RetryResult.success(function.apply(args));
            } catch (Exception e) {
                lastException = e;
                if(!retryConfig.isRetryable(e)) {
                    break;
                }
            }
        }
        return RetryResult.failure(lastException);
    }

    private Duration calculateDelay(int attempt) {
        if(attempt <= 1) {
            return Duration.ZERO;
        }
        double exponential = retryConfig.getInitialDelay().toMillis() * Math.pow(retryConfig.getMultiplier(), attempt-1);

        double jitter = 1.0 + (random.nextDouble()*2 - 1.0d)*retryConfig.getJitterFactor();

        long delaysInMillis = (long) (exponential*jitter);
        delaysInMillis = Math.min(delaysInMillis, retryConfig.getMaxDelay().toMillis());
        delaysInMillis = Math.max(0, delaysInMillis);
        return Duration.ofMillis(delaysInMillis);


    }


}
