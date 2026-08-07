package org.example.jobscheduler.scheduler;

import org.example.jobscheduler.entity.Job;
import org.example.jobscheduler.entity.Task;
import org.example.jobscheduler.entity.TaskStatus;
import org.example.jobscheduler.repository.TaskRepository;
import org.example.jobscheduler.service.CronService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskExecutionServiceTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final CronService cronService = mock(CronService.class);
    private final TaskExecutionService executionService = new TaskExecutionService(taskRepository, cronService);

    @Test
    void startTask_withQueuedTask_marksInProgressAndReturnsContext() {
        Job job = job(true);
        Task task = task(job, TaskStatus.QUEUED);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        Optional<TaskExecutionContext> context = executionService.startTask(10L);

        assertThat(context).isPresent();
        assertThat(context.get().message()).isEqualTo("hello");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void startTask_withNonQueuedTask_returnsEmpty() {
        Job job = job(true);
        Task task = task(job, TaskStatus.COMPLETED);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThat(executionService.startTask(10L)).isEmpty();
    }

    @Test
    void startTask_withMissingTask_returnsEmpty() {
        when(taskRepository.findById(10L)).thenReturn(Optional.empty());

        assertThat(executionService.startTask(10L)).isEmpty();
    }

    @Test
    void completeTask_successOnActiveJob_marksCompletedAndCreatesNextQueuedTask() {
        Job job = job(true);
        Task task = task(job, TaskStatus.IN_PROGRESS);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        LocalDateTime next = LocalDateTime.now().plusHours(1);
        when(cronService.nextExecutionTime(any(), any())).thenReturn(next);

        executionService.completeTask(10L, true);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void completeTask_failureOnInactiveJob_marksFailedAndDoesNotCreateNextTask() {
        Job job = job(false);
        Task task = task(job, TaskStatus.IN_PROGRESS);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        executionService.completeTask(10L, false);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void completeTask_withMissingTask_doesNothing() {
        when(taskRepository.findById(10L)).thenReturn(Optional.empty());

        executionService.completeTask(10L, true);

        verify(taskRepository, never()).save(any(Task.class));
    }

    private static Job job(boolean active) {
        Job job = new Job("desc", "hello", 2, "0 0 * * * *");
        setId(job, 1L);
        job.setActive(active);
        return job;
    }

    private static Task task(Job job, TaskStatus status) {
        Task task = new Task(job, status, LocalDateTime.now(), 2);
        setId(task, 10L);
        return task;
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