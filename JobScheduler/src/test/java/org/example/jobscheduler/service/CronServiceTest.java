package org.example.jobscheduler.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CronServiceTest {

    private final CronService cronService = new CronService();

    @Test
    void nextExecutionTime_computesNextFireTimeForValidCron() {
        LocalDateTime from = LocalDateTime.of(2026, Month.JANUARY, 1, 10, 30, 0);

        LocalDateTime next = cronService.nextExecutionTime("0 0 * * * *", from);

        assertThat(next).isEqualTo(LocalDateTime.of(2026, Month.JANUARY, 1, 11, 0, 0));
    }

    @Test
    void nextExecutionTime_throwsForInvalidCron() {
        assertThatThrownBy(() -> cronService.nextExecutionTime("not a cron", LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parse_throwsForInvalidCron() {
        assertThatThrownBy(() -> cronService.parse("*"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}