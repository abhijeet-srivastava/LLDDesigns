package org.example.jobscheduler.dto;

public record InterruptResponse(Long jobId, boolean interrupted) {
}