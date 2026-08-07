package org.example.jobscheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jobscheduler")
public class SchedulerProperties {

    private final Poller poller = new Poller();
    private final Worker worker = new Worker();

    public Poller getPoller() {
        return poller;
    }

    public Worker getWorker() {
        return worker;
    }

    public static class Poller {
        private long fixedDelayMs = 15000;
        private long lookaheadMinutes = 1;

        public long getFixedDelayMs() {
            return fixedDelayMs;
        }

        public void setFixedDelayMs(long fixedDelayMs) {
            this.fixedDelayMs = fixedDelayMs;
        }

        public long getLookaheadMinutes() {
            return lookaheadMinutes;
        }

        public void setLookaheadMinutes(long lookaheadMinutes) {
            this.lookaheadMinutes = lookaheadMinutes;
        }
    }

    public static class Worker {
        private int poolSize = 4;

        public int getPoolSize() {
            return poolSize;
        }

        public void setPoolSize(int poolSize) {
            this.poolSize = poolSize;
        }
    }
}