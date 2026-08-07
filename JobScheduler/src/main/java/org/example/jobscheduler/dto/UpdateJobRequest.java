package org.example.jobscheduler.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateJobRequest(
        String description,
        String message,
        @Min(value = 1, message = "priority must be between 1 and 5")
        @Max(value = 5, message = "priority must be between 1 and 5") Integer priority,
        String cronstatement,
        Boolean active) {
}