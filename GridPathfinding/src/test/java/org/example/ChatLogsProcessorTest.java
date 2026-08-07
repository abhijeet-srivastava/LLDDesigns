package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatLogsProcessor")
class ChatLogsProcessorTest {

    private ChatLogsProcessor processor;
    @BeforeEach
    void setUp() {
        processor = new ChatLogsProcessor();
    }

    @Test
    @DisplayName("Most attarctive user with no register event")
    void attarctiveUserInEmptyChats() {
        String attractiveUser = processor.getMostActiveUser(0);
        assertThat(attractiveUser).isEmpty();
    }

    @Test
    @DisplayName("Most attarctive user with one register event")
    void attarctiveUserWithOneEvent() {
        processor.registerEvent("alice", "bob", 0);
        String attractiveUser = processor.getMostActiveUser(0);
        assertThat(attractiveUser).isEqualTo("alice");
    }

    @Test
    @DisplayName("Most attarctive user with more then one register event")
    void attarctiveUserWithMultipleEvent() {
        processor.registerEvent("alice", "bob", 0);
        processor.registerEvent("john", "bob", 1);
        processor.registerEvent("alice", "ram", 1);
        processor.registerEvent("john", "bob", 2);
        //processor.registerEvent("jenny", "bob", 1);
        String attractiveUser = processor.getMostActiveUser(2);
        assertThat(attractiveUser).isEqualTo("alice");
    }

    @Test
    @DisplayName("Most attarctive user with more then one register event")
    void attarctiveUserWithMultipleEventsFromCG() {
        processor.registerEvent("alice", "bob", 1);
        processor.registerEvent("bob", "alice", 2);
        processor.registerEvent("bob", "charlie", 3);
        //processor.registerEvent("jenny", "bob", 1);
        String attractiveUser = processor.getMostActiveUser(4);
        assertThat(attractiveUser).isEqualTo("bob");
    }
    @Test
    @DisplayName("Most attarctive user with more then one register event")
    void attarctiveUserWithMultipleEventsFromCG1() {
        processor.registerEvent("david", "emma", 10);
        processor.registerEvent("alice", "bob", 10);
        processor.registerEvent("alice", "charlie", 15);
        processor.registerEvent("bob", "charlie", 20);
        //processor.registerEvent("jenny", "bob", 1);
        String attractiveUser = processor.getMostActiveUser(25);
        assertThat(attractiveUser).isEqualTo("alice");
    }

}