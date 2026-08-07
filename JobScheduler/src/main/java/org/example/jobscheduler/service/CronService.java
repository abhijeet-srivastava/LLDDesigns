package org.example.jobscheduler.service;

import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CronService {

    public LocalDateTime nextExecutionTime(String cron, LocalDateTime from) {
        CronExpression expression = parse(cron);
        LocalDateTime next = expression.next(from);
        if (next == null) {
            throw new IllegalArgumentException("Cron expression has no future execution time: " + cron);
        }
        return next;
    }

    public CronExpression parse(String cron) {
        try {
            return CronExpression.parse(cron);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid cron expression: " + cron, e);
        }
    }
}