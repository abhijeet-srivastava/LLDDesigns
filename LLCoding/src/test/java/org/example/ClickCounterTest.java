package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClickCounter")
class ClickCounterTest {
    @Test
    @DisplayName("Validate getRecentClicks")
    void testGetRecentClicks() {
        ClickCounter tracker = new ClickCounter();
        //register a click at time 1
        tracker.recordClick(1);
        //register a click at time 2
        tracker.recordClick(2);
        //register a click at time 3
        tracker.recordClick(3);
        //retrieve clicks at time 4, expect 3
        assertThat(tracker.getRecentClicks(4)).isEqualTo(3);
        //register a click at time 300
        tracker.recordClick(300);
        //retrieve clicks at time 300, expect 4
        assertThat(tracker.getRecentClicks(300)).isEqualTo(4);
        //retrieve clicks at time 301, expect 3
        assertThat(tracker.getRecentClicks(301)).isEqualTo(3);
    }

    @Test
    @DisplayName("Validate PhoneDirectory")
    void testPhoneDirectory() {
        // Initialize a number registry with 4 numbers: 0, 1, 2, and 3.
        NumberRegistry registry = new NumberRegistry(4);

        // May return any unused number. Suppose it returns 1.
        registry.fetch();
        // Suppose it returns 0.
        registry.fetch();

        // Number 2 is still available, so this returns true.
        assertThat(registry.isAvailable(2)).isTrue();

        // Only number 2 is left, so this call returns 2.
        assertThat(registry.fetch()).isEqualTo(2);

        // Number 3 is still available, so this returns true.
        assertThat(registry.isAvailable(3)).isTrue();

        // Release the number 2 back to the registry.
        registry.returnNumber(2);

        // Now, number 2 is available again, so this returns true.
        assertThat(registry.isAvailable(2)).isTrue();
    }


}