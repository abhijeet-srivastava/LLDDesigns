package org.example.jobscheduler.service;

import org.example.jobscheduler.config.TaskExecutionRegistry;
import org.example.jobscheduler.dto.CreateJobRequest;
import org.example.jobscheduler.dto.InterruptResponse;
import org.example.jobscheduler.dto.JobResponse;
import org.example.jobscheduler.dto.UpdateJobRequest;
import org.example.jobscheduler.entity.Job;
import org.example.jobscheduler.entity.Task;
import org.example.jobscheduler.entity.TaskStatus;
import org.example.jobscheduler.exception.JobNotFoundException;
import org.example.jobscheduler.repository.JobRepository;
import org.example.jobscheduler.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobServiceTest {

    private final JobRepository jobRepository = mock(JobRepository.class);
    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final CronService cronService = mock(CronService.class);
    private final TaskExecutionRegistry registry = new TaskExecutionRegistry();

    private final JobService jobService = new JobService(jobRepository, taskRepository, cronService, registry);

    @BeforeEach
    void setUp() {
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            setId(job, 1L);
            return job;
        });
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            setId(task, 10L);
            return task;
        });
    }

    @Test
    void createJob_persistsJobAndQueuedTaskWithComputedNextExecutionTime() {
        LocalDateTime next = LocalDateTime.now().plusHours(1);
        when(cronService.nextExecutionTime(any(), any())).thenReturn(next);
        CreateJobRequest request = new CreateJobRequest("desc", "hello", 2, "0 0 * * * *");

        JobResponse response = jobService.createJob(request);

        assertThat(response.jobId()).isEqualTo(1L);
        assertThat(response.jobStatus()).isEqualTo("QUEUED");
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.QUEUED);
        assertThat(taskCaptor.getValue().getNextExecutionTime()).isEqualTo(next);
    }

    @Test
    void createJob_withInvalidCron_throwsAndPersistsNothing() {
        when(cronService.parse(any())).thenThrow(new IllegalArgumentException("bad cron"));
        CreateJobRequest request = new CreateJobRequest("desc", "hello", 2, "garbage");

        assertThatThrownBy(() -> jobService.createJob(request)).isInstanceOf(IllegalArgumentException.class);

        verify(jobRepository, never()).save(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void getJob_withUnknownId_throwsJobNotFoundException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJob(99L)).isInstanceOf(JobNotFoundException.class);
    }

    @Test
    void updateJob_withNewCron_recomputesPendingQueuedTaskNextExecutionTime() {
        Job job = new Job("desc", "hello", 2, "0 0 * * * *");
        setId(job, 1L);
        Task queuedTask = new Task(job, TaskStatus.QUEUED, LocalDateTime.now(), 2);
        setId(queuedTask, 10L);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(taskRepository.findFirstByJobIdAndStatus(1L, TaskStatus.QUEUED)).thenReturn(Optional.of(queuedTask));
        LocalDateTime newNext = LocalDateTime.now().plusDays(1);
        when(cronService.nextExecutionTime(any(), any())).thenReturn(newNext);

        jobService.updateJob(1L, new UpdateJobRequest(null, null, null, "0 0 0 * * *", null));

        assertThat(job.getCron()).isEqualTo("0 0 0 * * *");
        assertThat(queuedTask.getNextExecutionTime()).isEqualTo(newNext);
    }

    @Test
    void updateJob_withUnknownId_throwsJobNotFoundException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.updateJob(99L, new UpdateJobRequest("d", null, null, null, null)))
                .isInstanceOf(JobNotFoundException.class);
    }

    @Test
    void deleteJob_removesTasksThenJob() {
        Job job = new Job("desc", "hello", 2, "0 0 * * * *");
        setId(job, 1L);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        jobService.deleteJob(1L);

        verify(taskRepository, times(1)).deleteByJobId(1L);
        verify(jobRepository, times(1)).delete(job);
    }

    @Test
    void deleteJob_withUnknownId_throwsJobNotFoundException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.deleteJob(99L)).isInstanceOf(JobNotFoundException.class);
        verify(taskRepository, never()).deleteByJobId(anyLong());
    }

    @Test
    void interruptJob_withRunningTask_interruptsThreadAndReturnsTrue() throws InterruptedException {
        Job job = new Job("desc", "hello", 2, "0 0 * * * *");
        setId(job, 1L);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
                // expected
            }
        });
        worker.start();
        registry.register(1L, worker);

        InterruptResponse response = jobService.interruptJob(1L);

        assertThat(response.interrupted()).isTrue();
        worker.join(1000);
        assertThat(worker.isAlive()).isFalse();
    }

    @Test
    void interruptJob_withNoRunningTask_returnsFalse() {
        Job job = new Job("desc", "hello", 2, "0 0 * * * *");
        setId(job, 1L);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        InterruptResponse response = jobService.interruptJob(1L);

        assertThat(response.interrupted()).isFalse();
    }

    @Test
    void interruptJob_withUnknownJobId_throwsJobNotFoundException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.interruptJob(99L)).isInstanceOf(JobNotFoundException.class);
    }

    private static void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}