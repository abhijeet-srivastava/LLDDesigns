package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MeetingRoomScheduler")
class MeetingRoomSchedulerTest {

    @Nested
    @DisplayName("recurringMeetingTest")
    class  RecurringMeetingTest {
        MeetingRoomScheduler scheduler;
        @BeforeEach
        void setUp() {
            scheduler = new MeetingRoomScheduler(2, 2);
        }

        @Test
        @DisplayName("Test recurring meetings")
        void testRecurringMeetings() {
            List<Integer> availableRooms = scheduler.getAvailableRooms(10, 12);
            assertThat(availableRooms).containsExactly(0, 1);

            boolean bookingRes = scheduler.bookRoom("b1", 0, 0, 10, 3, 5);
            assertThat(bookingRes).isEqualTo(true);

            availableRooms = scheduler.getAvailableRooms(12, 12);
            assertThat(availableRooms).containsExactly(1);
            bookingRes = scheduler.bookRoom("b2", 1, 0, 12, 1, 10);
            assertThat(bookingRes).isEqualTo(false);

            bookingRes = scheduler.bookRoom("b3", 1, 0, 16, 1, 10);
            assertThat(bookingRes).isEqualTo(false);

            bookingRes = scheduler.bookRoom("b4", 1, 1, 12, 2, 6);
            assertThat(bookingRes).isEqualTo(true);

            assertThat(scheduler.getAvailableRooms(12, 12)).isEmpty();

            List<String> roomBookings = scheduler.listBookingsForRoom(0, 5);
            assertThat(roomBookings).containsExactly("b1-10-12", "b1-15-17", "b1-20-22", "b1-25-27", "b1-30-32");

            roomBookings = scheduler.listBookingsForRoom(1,4);
            assertThat(roomBookings).containsExactly("b4-12-13", "b4-18-19", "b4-24-25", "b4-30-31");

            roomBookings = scheduler.listBookingsForEmployee(0, 3);
            assertThat(roomBookings).containsExactly("b1-10-12", "b1-15-17", "b1-20-22");

            roomBookings = scheduler.listBookingsForEmployee(1, 3);
            assertThat(roomBookings).containsExactly("b4-12-13", "b4-18-19", "b4-24-25");

            bookingRes = scheduler.cancelBooking("b1");

            assertThat(bookingRes).isEqualTo(true);

            availableRooms = scheduler.getAvailableRooms(12, 12);
            assertThat(availableRooms).containsExactly(0);

            bookingRes = scheduler.cancelBooking("b1");
            assertThat(bookingRes).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("TestInvalidInput")
    class  TestInvalidInput {
        MeetingRoomScheduler s;
        @BeforeEach
        void setUp() {
            s = new MeetingRoomScheduler(1, 1);
        }

        @Test
        @DisplayName("Test invalid inputs")
        void testInvalidInputs() {
            assertThat(s.listBookingsForRoom(0, 5)).isEmpty();
            assertThat(s.listBookingsForEmployee(0, 5)).isEmpty();
            assertThat(s.bookRoom("x1", 0, 0, -1, 1, 5)).isEqualTo(false);
            assertThat(s.bookRoom("x2", 0, 0, 10, 0, 5)).isEqualTo(false);
            assertThat(s.bookRoom("x3", 0, 0, 10, 5, 5)).isEqualTo(false);
            assertThat(s.bookRoom("x4", 0, 0, 20, 1, 7)).isEqualTo(true);
            assertThat(s.getAvailableRooms(30, 10)).isEmpty();
            assertThat(s.getAvailableRooms(20, 20)).isEmpty();
            assertThat(s.bookRoom("x5", 0, 0, 21, 1, 8)).isFalse();
            assertThat(s.listBookingsForRoom(0, 6)).containsExactly("x4-20-20", "x4-27-27", "x4-34-34", "x4-41-41", "x4-48-48", "x4-55-55");
            assertThat(s.cancelBooking("not-exists")).isFalse();
            assertThat(s.cancelBooking("x4")).isTrue();
            assertThat(s.getAvailableRooms(20, 20)).containsExactly(0);
        }
    }

}