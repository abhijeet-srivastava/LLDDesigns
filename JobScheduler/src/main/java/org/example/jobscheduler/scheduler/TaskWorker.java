package org.example.jobscheduler.scheduler;

import jakarta.annotation.PostConstruct;
import org.example.jobscheduler.config.TaskExecutionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;

@Component
public class TaskWorker {

    private static final Logger log = LoggerFactory.getLogger(TaskWorker.class);

    private final PriorityBlockingQueue<TaskRef> taskQueue;
    private final Set<Long> dispatchedTaskIds;
    private final ExecutorService workerExecutor;
    private final TaskExecutionService taskExecutionService;
    private final TaskExecutionRegistry registry;
    private final int poolSize;

    public TaskWorker(PriorityBlockingQueue<TaskRef> taskQueue,
                       Set<Long> dispatchedTaskIds,
                       ExecutorService workerExecutor,
                       TaskExecutionService taskExecutionService,
                       TaskExecutionRegistry registry,
                       org.example.jobscheduler.config.SchedulerProperties properties) {
        this.taskQueue = taskQueue;
        this.dispatchedTaskIds = dispatchedTaskIds;
        this.workerExecutor = workerExecutor;
        this.taskExecutionService = taskExecutionService;
        this.registry = registry;
        this.poolSize = properties.getWorker().getPoolSize();
    }

    @PostConstruct
    public void start() {
        for (int i = 0; i < poolSize; i++) {
            workerExecutor.submit(this::runLoop);
        }
    }

    private void runLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TaskRef ref = taskQueue.take();
                dispatchedTaskIds.remove(ref.taskId());
                execute(ref);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void execute(TaskRef ref) {
        taskExecutionService.startTask(ref.taskId()).ifPresent(context -> {
            Thread current = Thread.currentThread();
            registry.register(context.jobId(), current);
            boolean success;
            try {
                log.info("Executing task {} for job {}: {}", context.taskId(), context.jobId(), context.message());
                Thread.sleep(1);
                success = true;
            } catch (InterruptedException e) {
                // Interrupt was targeted at this task's execution (see interrupt endpoint), not
                // at the worker thread itself, so the flag is intentionally left cleared here —
                // the worker loops back around to pick up the next task.
                success = false;
            } catch (RuntimeException e) {
                success = false;
            } finally {
                registry.unregister(context.jobId(), current);
            }
            taskExecutionService.completeTask(context.taskId(), success);
        });
    }
}