package org.example.jobscheduler.config;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TaskExecutionRegistry {

    private final ConcurrentHashMap<Long, Thread> runningByJobId = new ConcurrentHashMap<>();

    public void register(Long jobId, Thread thread) {
        runningByJobId.put(jobId, thread);
    }

    public void unregister(Long jobId, Thread thread) {
        runningByJobId.remove(jobId, thread);
    }

    public Optional<Thread> find(Long jobId) {
        return Optional.ofNullable(runningByJobId.get(jobId));
    }
}