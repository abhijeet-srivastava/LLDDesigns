package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ChatLogsProcessorV2")
class ChatLogsProcessorV2Test {

    private ChatLogsProcessorV2 processor;

    @BeforeEach
    void setUp() {
        processor = new ChatLogsProcessorV2();
    }

    @Test
    @DisplayName("Most active user with no register event")
    void mostActiveUserInEmptyChats() {
        String mostActiveUser = processor.getMostActiveUser(0);
        assertThat(mostActiveUser).isEmpty();
    }

    @Test
    @DisplayName("Most active user with one register event")
    void mostActiveUserWithOneEvent() {
        processor.registerEvent("alice", "bob", 0);
        String mostActiveUser = processor.getMostActiveUser(0);
        assertThat(mostActiveUser).isEqualTo("alice");
    }

    @Test
    @DisplayName("Most active user with more than one register event")
    void mostActiveUserWithMultipleEvents() {
        processor.registerEvent("alice", "bob", 0);
        processor.registerEvent("john", "bob", 1);
        processor.registerEvent("alice", "ram", 1);
        processor.registerEvent("john", "bob", 2);
        String mostActiveUser = processor.getMostActiveUser(2);
        assertThat(mostActiveUser).isEqualTo("alice");
    }

    @Test
    @DisplayName("Most active user across multiple events including a reversed-order duplicate")
    void mostActiveUserWithReversedDuplicatePair() {
        processor.registerEvent("alice", "bob", 1);
        processor.registerEvent("bob", "alice", 2);
        processor.registerEvent("bob", "charlie", 3);
        String mostActiveUser = processor.getMostActiveUser(4);
        assertThat(mostActiveUser).isEqualTo("bob");
    }

    @Test
    @DisplayName("Most active user with tie broken lexicographically")
    void mostActiveUserWithTieBreak() {
        processor.registerEvent("david", "emma", 10);
        processor.registerEvent("alice", "bob", 10);
        processor.registerEvent("alice", "charlie", 15);
        processor.registerEvent("bob", "charlie", 20);
        String mostActiveUser = processor.getMostActiveUser(25);
        assertThat(mostActiveUser).isEqualTo("alice");
    }

    @Test
    @DisplayName("Reversed sender/receiver pair is deduped regardless of char-distance between names")
    void reversedPairWithSingleCharDistanceIsDeduped() {
        processor.registerEvent("charlie", "bob", 1);
        processor.registerEvent("bob", "charlie", 2);
        String mostActiveUser = processor.getMostActiveUser(2);
        assertThat(mostActiveUser).isEqualTo("bob");
        assertThat(processor.getMostActiveUser(2)).isNotEqualTo("charlie");
    }

    @Test
    @DisplayName("Two distinct new pairs registered at the same timestamp are both counted")
    void distinctPairsAtSameTimestampAreNotLost() {
        processor.registerEvent("alice", "bob", 5);
        processor.registerEvent("john", "ram", 5);
        String mostActiveUser = processor.getMostActiveUser(5);
        assertThat(mostActiveUser).isEqualTo("alice");
    }

    @Test
    @DisplayName("Querying with a currentTime earlier than a previous query throws")
    void nonMonotonicQueryTimeThrows() {
        processor.registerEvent("alice", "bob", 5);
        processor.getMostActiveUser(10);
        assertThatThrownBy(() -> processor.getMostActiveUser(3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Events registered after currentTime are not counted until queried at/after their timestamp")
    void futureEventsAreNotCountedEarly() {
        processor.registerEvent("alice", "bob", 10);
        String mostActiveUser = processor.getMostActiveUser(5);
        assertThat(mostActiveUser).isEmpty();

        mostActiveUser = processor.getMostActiveUser(10);
        assertThat(mostActiveUser).isEqualTo("alice");
    }
}