package org.example.jobscheduler.scheduler;

import org.example.jobscheduler.config.SchedulerProperties;
import org.example.jobscheduler.entity.Job;
import org.example.jobscheduler.entity.Task;
import org.example.jobscheduler.entity.TaskStatus;
import org.example.jobscheduler.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaskPollerServiceTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final PriorityBlockingQueue<TaskRef> queue = new PriorityBlockingQueue<>();
    private final Set<Long> dispatchedTaskIds = ConcurrentHashMap.newKeySet();
    private final SchedulerProperties properties = new SchedulerProperties();

    private final TaskPollerService pollerService =
            new TaskPollerService(taskRepository, queue, dispatchedTaskIds, properties);

    @BeforeEach
    void setUp() {
        properties.getPoller().setLookaheadMinutes(1);
    }

    @Test
    void poll_enqueuesDueTaskAndMarksDispatched() {
        Task task = task(10L, 5L, TaskStatus.QUEUED);
        when(taskRepository.findDueTasks(any())).thenReturn(List.of(task));

        pollerService.poll();

        assertThat(queue).hasSize(1);
        assertThat(queue.peek().taskId()).isEqualTo(10L);
        assertThat(dispatchedTaskIds).contains(10L);
    }

    @Test
    void poll_doesNotReenqueueAlreadyDispatchedTask() {
        Task task = task(10L, 5L, TaskStatus.QUEUED);
        when(taskRepository.findDueTasks(any())).thenReturn(List.of(task));

        pollerService.poll();
        pollerService.poll();

        assertThat(queue).hasSize(1);
    }

    @Test
    void poll_withNoDueTasks_leavesQueueEmpty() {
        when(taskRepository.findDueTasks(any())).thenReturn(List.of());

        pollerService.poll();

        assertThat(queue).isEmpty();
        assertThat(dispatchedTaskIds).isEmpty();
    }

    private static Task task(Long taskId, Long jobId, TaskStatus status) {
        Job job = new Job("desc", "hello", 2, "0 0 * * * *");
        setId(job, jobId);
        Task task = new Task(job, status, LocalDateTime.now(), 2);
        setId(task, taskId);
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