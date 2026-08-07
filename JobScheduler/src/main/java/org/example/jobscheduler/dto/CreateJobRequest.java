package org.example.jobscheduler.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateJobRequest(
        String description,
        @NotBlank(message = "message must not be blank") String message,
        @Min(value = 1, message = "priority must be between 1 and 5")
        @Max(value = 5, message = "priority must be between 1 and 5") int priority,
        @NotBlank(message = "cronstatement must not be blank") String cronstatement) {
}