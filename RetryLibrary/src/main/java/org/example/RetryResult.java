package org.example;

public class RetryResult<T> {

    private final boolean success;
    private final T value;
    private final Throwable exception;

    private RetryResult(boolean success, T value, Throwable exception) {
        this.success = success;
        this.value = value;
        this.exception = exception;
    }

    public static <T> RetryResult<T> success(T value) {
        return new RetryResult<>(true, value, null);
    }

    public static <T> RetryResult<T> failure(Throwable exception) {
        return new RetryResult<>(false, null, exception);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getValue() {
        return value;
    }

    public Throwable getException() {
        return exception;
    }
}