package org.example.jobscheduler.service;

import org.example.jobscheduler.config.TaskExecutionRegistry;
import org.example.jobscheduler.dto.CreateJobRequest;
import org.example.jobscheduler.dto.InterruptResponse;
import org.example.jobscheduler.dto.JobDetailsResponse;
import org.example.jobscheduler.dto.JobResponse;
import org.example.jobscheduler.dto.UpdateJobRequest;
import org.example.jobscheduler.entity.Job;
import org.example.jobscheduler.entity.Task;
import org.example.jobscheduler.entity.TaskStatus;
import org.example.jobscheduler.exception.JobNotFoundException;
import org.example.jobscheduler.repository.JobRepository;
import org.example.jobscheduler.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final TaskRepository taskRepository;
    private final CronService cronService;
    private final TaskExecutionRegistry registry;

    public JobService(JobRepository jobRepository, TaskRepository taskRepository,
                       CronService cronService, TaskExecutionRegistry registry) {
        this.jobRepository = jobRepository;
        this.taskRepository = taskRepository;
        this.cronService = cronService;
        this.registry = registry;
    }

    @Transactional
    public JobResponse createJob(CreateJobRequest request) {
        cronService.parse(request.cronstatement());
        Job job = new Job(request.description(), request.message(), request.priority(), request.cronstatement());
        job = jobRepository.save(job);
        LocalDateTime next = cronService.nextExecutionTime(job.getCron(), LocalDateTime.now());
        Task task = taskRepository.save(new Task(job, TaskStatus.QUEUED, next, job.getPriority()));
        return new JobResponse(job.getId(), job.getMessage(), task.getStatus().name());
    }

    @Transactional(readOnly = true)
    public JobDetailsResponse getJob(Long jobId) {
        Job job = findJobOrThrow(jobId);
        return toDetails(job);
    }

    @Transactional
    public JobDetailsResponse updateJob(Long jobId, UpdateJobRequest request) {
        Job job = findJobOrThrow(jobId);

        if (request.description() != null) {
            job.setDescription(request.description());
        }
        if (request.message() != null) {
            job.setMessage(request.message());
        }
        if (request.priority() != null) {
            job.setPriority(request.priority());
        }
        if (request.active() != null) {
            job.setActive(request.active());
        }
        if (request.cronstatement() != null) {
            cronService.parse(request.cronstatement());
            job.setCron(request.cronstatement());
            taskRepository.findFirstByJobIdAndStatus(jobId, TaskStatus.QUEUED).ifPresent(task -> {
                task.setNextExecutionTime(cronService.nextExecutionTime(job.getCron(), LocalDateTime.now()));
                task.setPriority(job.getPriority());
            });
        }
        return toDetails(job);
    }

    @Transactional
    public void deleteJob(Long jobId) {
        Job job = findJobOrThrow(jobId);
        registry.find(jobId).ifPresent(Thread::interrupt);
        taskRepository.deleteByJobId(jobId);
        jobRepository.delete(job);
    }

    @Transactional
    public InterruptResponse interruptJob(Long jobId) {
        findJobOrThrow(jobId);
        boolean interrupted = registry.find(jobId)
                .map(thread -> {
                    thread.interrupt();
                    return true;
                })
                .orElse(false);
        return new InterruptResponse(jobId, interrupted);
    }

    private Job findJobOrThrow(Long jobId) {
        return jobRepository.findById(jobId).orElseThrow(() -> new JobNotFoundException(jobId));
    }

    private JobDetailsResponse toDetails(Job job) {
        return taskRepository.findFirstByJobIdAndStatus(job.getId(), TaskStatus.IN_PROGRESS)
                .or(() -> taskRepository.findFirstByJobIdAndStatus(job.getId(), TaskStatus.QUEUED))
                .map(task -> new JobDetailsResponse(job.getId(), job.getDescription(), job.getMessage(),
                        job.getPriority(), job.getCron(), job.isActive(), task.getId(), task.getStatus().name(),
                        task.getNextExecutionTime()))
                .orElseGet(() -> new JobDetailsResponse(job.getId(), job.getDescription(), job.getMessage(),
                        job.getPriority(), job.getCron(), job.isActive(), null, null, null));
    }
}