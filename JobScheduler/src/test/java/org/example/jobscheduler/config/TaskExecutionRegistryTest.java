package org.example.jobscheduler.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskExecutionRegistryTest {

    private final TaskExecutionRegistry registry = new TaskExecutionRegistry();

    @Test
    void registerThenFind_returnsRegisteredThread() {
        Thread thread = new Thread();

        registry.register(1L, thread);

        assertThat(registry.find(1L)).contains(thread);
    }

    @Test
    void unregister_removesMatchingThread() {
        Thread thread = new Thread();
        registry.register(1L, thread);

        registry.unregister(1L, thread);

        assertThat(registry.find(1L)).isEmpty();
    }

    @Test
    void unregister_withStaleThread_doesNotRemoveNewerRegistration() {
        Thread first = new Thread();
        Thread second = new Thread();
        registry.register(1L, first);
        registry.register(1L, second);

        registry.unregister(1L, first);

        assertThat(registry.find(1L)).contains(second);
    }

    @Test
    void find_withUnknownJobId_isEmpty() {
        assertThat(registry.find(99L)).isEmpty();
    }
}