package org.example.jobscheduler.exception;

public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(Long jobId) {
        super("Job not found: " + jobId);
    }
}