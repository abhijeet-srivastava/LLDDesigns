package org.example.jobscheduler.repository;

import org.example.jobscheduler.entity.Task;
import org.example.jobscheduler.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.status = org.example.jobscheduler.entity.TaskStatus.QUEUED "
            + "AND t.job.active = true AND t.nextExecutionTime <= :cutoff")
    List<Task> findDueTasks(@Param("cutoff") LocalDateTime cutoff);

    Optional<Task> findFirstByJobIdAndStatus(Long jobId, TaskStatus status);

    void deleteByJobId(Long jobId);
}