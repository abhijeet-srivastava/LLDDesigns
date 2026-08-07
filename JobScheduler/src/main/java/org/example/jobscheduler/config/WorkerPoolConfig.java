package org.example.jobscheduler.config;

import org.example.jobscheduler.scheduler.TaskRef;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

@Configuration
public class WorkerPoolConfig {

    @Bean
    public PriorityBlockingQueue<TaskRef> taskQueue() {
        return new PriorityBlockingQueue<>();
    }

    @Bean
    public Set<Long> dispatchedTaskIds() {
        return ConcurrentHashMap.newKeySet();
    }

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService workerExecutor(@Value("${jobscheduler.worker.pool-size:4}") int poolSize) {
        return Executors.newFixedThreadPool(poolSize);
    }
}