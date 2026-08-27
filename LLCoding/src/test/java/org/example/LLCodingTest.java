package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LLCoding")
class LLCodingTest {
    Main llcoding;

    @BeforeEach
    void setUp() {
        llcoding = new Main();
    }

    @Test
    @DisplayName("Validate countPrefixOccurrences")
    void testCountPrefixOccurrences() {
        assertThat(llcoding.countPrefixOccurrences(5, List.of("0 1", "1 2", "1 3", "3 4"), "ababa","aba"))
                .containsExactly(3,3,2);

        assertThat(llcoding.countPrefixOccurrences(4, List.of("0 1", "0 2", "0 3"),  "baca", "abc"))
                .containsExactly(2,2,2);

        assertThat(llcoding.countPrefixOccurrences(3, List.of("0 1", "1 2"), "aab", "aaa"))
                .containsExactly(2,2,0);
    }

    @Test
    @DisplayName("Validate firstTimedOutJobId")
    void testFirstTimedOutJobId() {
        assertThat(llcoding.firstTimedOutJobId(List.of("1,1,START", "2,2,START", "1,4,END", "3,8,START", "3,15,END"), 5))
                .isEqualTo(2);

        assertThat(llcoding.firstTimedOutJobId(List.of("5,1,START", "6,1,START", "5,7,END", "6,7,END"), 5))
                .isEqualTo(5);

        assertThat(llcoding.firstTimedOutJobId(List.of("10,2,START", "10,7,END", "20,8,START", "20,13,END"), 5))
                .isEqualTo(-1);

        assertThat(llcoding.firstTimedOutJobId(List.of("10,2,START", "10,7,END", "20,8,START", "20,13,END"), 5))
                .isEqualTo(-1);

        assertThat(llcoding.firstTimedOutJobId(List.of("1,1,START", "2,3,START", "3,7,START"), 4))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("firstTimedOutJobId: empty logs returns -1")
    void testFirstTimedOutJobId_emptyLogs() {
        assertThat(llcoding.firstTimedOutJobId(List.of(), 5))
                .isEqualTo(-1);
    }

    @Test
    @DisplayName("firstTimedOutJobId: elapsed exactly equal to threshold is NOT a timeout")
    void testFirstTimedOutJobId_exactThresholdBoundaryIsNotTimeout() {
        // diff == threshold uses '>' not '>=', so this must not be flagged
        assertThat(llcoding.firstTimedOutJobId(List.of("1,1,START", "1,6,END"), 5))
                .isEqualTo(-1);
    }

    @Test
    @DisplayName("firstTimedOutJobId: lone START with no follow-up log cannot be detected")
    void testFirstTimedOutJobId_loneStartNeverDetected() {
        // No later log advances the clock, so an unbounded-running job is invisible to the window check
        assertThat(llcoding.firstTimedOutJobId(List.of("1,1,START"), 5))
                .isEqualTo(-1);
    }

    @Test
    @DisplayName("firstTimedOutJobId: job that never ends is detected once a later log advances time past it")
    void testFirstTimedOutJobId_expiresWithoutEnd() {
        assertThat(llcoding.firstTimedOutJobId(List.of("1,1,START", "2,10,START"), 5))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("firstTimedOutJobId: a job that completed in time is not falsely flagged once window advances past it")
    void testFirstTimedOutJobId_completedJobNotFalselyFlaggedLater() {
        assertThat(llcoding.firstTimedOutJobId(List.of("1,1,START", "1,3,END", "2,20,START"), 5))
                .isEqualTo(-1);
    }

    @Test
    @DisplayName("firstTimedOutJobId: END with no matching START currently throws NPE (undefined input)")
    void testFirstTimedOutJobId_endWithoutStartThrowsNpe() {
        // jobStart.get(jobId) returns null for an unseen job; ts - null unboxes to NPE.
        // Documents current crash-on-malformed-input behavior rather than a graceful -1/exception.
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        llcoding.firstTimedOutJobId(List.of("1,5,END"), 5))
                .isInstanceOf(NullPointerException.class);
    }

}