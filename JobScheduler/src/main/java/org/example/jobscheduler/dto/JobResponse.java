package org.example.jobscheduler.dto;

public record JobResponse(Long jobId, String message, String jobStatus) {
}