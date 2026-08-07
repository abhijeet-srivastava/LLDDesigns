package org.example.jobscheduler.scheduler;

public record TaskExecutionContext(Long taskId, Long jobId, String message) {
}