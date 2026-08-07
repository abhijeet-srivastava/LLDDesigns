package org.example.jobscheduler.dto;

import java.time.LocalDateTime;

public record JobDetailsResponse(
        Long jobId,
        String description,
        String message,
        int priority,
        String cron,
        boolean active,
        Long currentTaskId,
        String currentTaskStatus,
        LocalDateTime nextExecutionTime) {
}