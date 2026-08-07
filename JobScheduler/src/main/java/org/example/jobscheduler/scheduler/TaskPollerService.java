package org.example.jobscheduler.scheduler;

import org.example.jobscheduler.config.SchedulerProperties;
import org.example.jobscheduler.entity.Task;
import org.example.jobscheduler.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

@Service
public class TaskPollerService {

    private static final Logger log = LoggerFactory.getLogger(TaskPollerService.class);

    private final TaskRepository taskRepository;
    private final PriorityBlockingQueue<TaskRef> taskQueue;
    private final Set<Long> dispatchedTaskIds;
    private final SchedulerProperties properties;

    public TaskPollerService(TaskRepository taskRepository,
                              PriorityBlockingQueue<TaskRef> taskQueue,
                              Set<Long> dispatchedTaskIds,
                              SchedulerProperties properties) {
        this.taskRepository = taskRepository;
        this.taskQueue = taskQueue;
        this.dispatchedTaskIds = dispatchedTaskIds;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${jobscheduler.poller.fixed-delay-ms:15000}")
    public void poll() {
        LocalDateTime cutoff = LocalDateTime.now().plusMinutes(properties.getPoller().getLookaheadMinutes());
        List<Task> dueTasks = taskRepository.findDueTasks(cutoff);
        for (Task task : dueTasks) {
            if (dispatchedTaskIds.add(task.getId())) {
                taskQueue.put(new TaskRef(task.getId(), task.getJob().getId(), task.getPriority(),
                        task.getNextExecutionTime()));
                log.debug("Dispatched task {} for job {}", task.getId(), task.getJob().getId());
            }
        }
    }
}