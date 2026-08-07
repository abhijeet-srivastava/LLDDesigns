package org.example.jobscheduler.scheduler;

import org.example.jobscheduler.entity.Job;
import org.example.jobscheduler.entity.Task;
import org.example.jobscheduler.entity.TaskStatus;
import org.example.jobscheduler.repository.TaskRepository;
import org.example.jobscheduler.service.CronService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TaskExecutionService {

    private final TaskRepository taskRepository;
    private final CronService cronService;

    public TaskExecutionService(TaskRepository taskRepository, CronService cronService) {
        this.taskRepository = taskRepository;
        this.cronService = cronService;
    }

    @Transactional
    public Optional<TaskExecutionContext> startTask(Long taskId) {
        return taskRepository.findById(taskId)
                .filter(task -> task.getStatus() == TaskStatus.QUEUED)
                .map(task -> {
                    task.setStatus(TaskStatus.IN_PROGRESS);
                    Job job = task.getJob();
                    return new TaskExecutionContext(task.getId(), job.getId(), job.getMessage());
                });
    }

    @Transactional
    public void completeTask(Long taskId, boolean success) {
        taskRepository.findById(taskId).ifPresent(task -> {
            task.setStatus(success ? TaskStatus.COMPLETED : TaskStatus.FAILED);
            Job job = task.getJob();
            if (job.isActive()) {
                LocalDateTime next = cronService.nextExecutionTime(job.getCron(), LocalDateTime.now());
                taskRepository.save(new Task(job, TaskStatus.QUEUED, next, job.getPriority()));
            }
        });
    }
}