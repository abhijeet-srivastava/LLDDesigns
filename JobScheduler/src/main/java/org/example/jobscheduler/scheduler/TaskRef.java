package org.example.jobscheduler.scheduler;

import java.time.LocalDateTime;

public record TaskRef(Long taskId, Long jobId, int priority, LocalDateTime nextExecutionTime)
        implements Comparable<TaskRef> {

    @Override
    public int compareTo(TaskRef other) {
        int byPriority = Integer.compare(this.priority, other.priority);
        if (byPriority != 0) {
            return byPriority;
        }
        return this.nextExecutionTime.compareTo(other.nextExecutionTime);
    }
}