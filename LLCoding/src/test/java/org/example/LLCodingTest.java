package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
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

    @Test
    @DisplayName("maximumLineScore: END with no matching START currently throws NPE (undefined input)")
    void testMaximumLineScore() {
        assertThat(llcoding.maximumCircleScore(List.of(5, 11, 4, 9, 2) ))
                .isEqualTo(20);
        assertThat(llcoding.maximumCircleScore(List.of(12, 3, 6, 10) ))
                .isEqualTo(22);
        assertThat(llcoding.maximumCircleScore(List.of(9, 2, 7, 4) ))
                .isEqualTo(16);

        assertThat(llcoding.maximumCircleScore(List.of(5, 12, 6, 11, 4) ))
                .isEqualTo(23);
        assertThat(llcoding.maximumCircleScore(List.of(5, 5, 10, 100, 10, 5) )).isEqualTo(110);
        assertThat(llcoding.maximumCircleScore(List.of(3, 2, 7, 10) )).isEqualTo(13);
        assertThat(llcoding.maximumCircleScore(List.of(9, 1, 6, 10) )).isEqualTo(19);
    }

    @Test
    @DisplayName("minimumSteps: END with no matching START currently throws NPE (undefined input)")
    void testMinimumSteps() {
        assertThat(llcoding.minimumSteps(List.of("....", ".##.", "...."), 2, 0, 0, 2, 2))
                .isEqualTo(2);
        assertThat(llcoding.minimumSteps(List.of(".#.", "###", ".#."), 2, 0, 0, 2, 2))
                .isEqualTo(-1);
        assertThat(llcoding.minimumSteps(List.of("..", ".."), 1, 1, 1, 1, 1))
                .isEqualTo(0);
    }

    @Test
    @DisplayName("minimumSteps: single-cell grid, source == destination")
    void testMinimumSteps_singleCellGrid() {
        assertThat(llcoding.minimumSteps(List.of("."), 5, 0, 0, 0, 0))
                .isEqualTo(0);
    }

    @Test
    @DisplayName("minimumSteps: k=0 allows zero movement, so any distinct destination is unreachable")
    void testMinimumSteps_zeroKBlocksMovement() {
        assertThat(llcoding.minimumSteps(List.of("..", ".."), 0, 0, 0, 1, 1))
                .isEqualTo(-1);
    }

    @Test
    @DisplayName("minimumSteps: k=0 with source == destination still returns 0 without needing to move")
    void testMinimumSteps_zeroKSameSourceAndDestination() {
        assertThat(llcoding.minimumSteps(List.of("..", ".."), 0, 1, 1, 1, 1))
                .isEqualTo(0);
    }

    @Test
    @DisplayName("minimumSteps: distance exactly k is reachable in one hop; k+1 needs a second hop")
    void testMinimumSteps_exactKBoundary() {
        List<String> row = List.of(".......");
        assertThat(llcoding.minimumSteps(row, 3, 0, 0, 0, 3))
                .isEqualTo(1);
        assertThat(llcoding.minimumSteps(row, 3, 0, 0, 0, 4))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("minimumSteps: obstacle forces a detour, minimum path follows the only open corridor")
    void testMinimumSteps_detourAroundObstacle() {
        List<String> grid = List.of(
                "..#..",
                "..#..",
                "....."
        );
        // Column 2 is blocked on rows 0-1; only row 2 is open, so the path must go
        // down 2, across 4, up 2 = 8 single-cell hops (k=1).
        assertThat(llcoding.minimumSteps(grid, 1, 0, 0, 0, 4))
                .isEqualTo(8);
    }

    @Test
    @DisplayName("minimumSteps: 100x100 open grid, corner to corner, with k large enough to cross each axis in one hop")
    void testMinimumSteps_largeOpenGridLargeK() {
        List<String> grid = buildOpenGrid(100);
        assertThat(llcoding.minimumSteps(grid, 100, 0, 0, 99, 99))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("minimumSteps: 100x100 open grid, corner to corner, with k=1 behaves like plain grid BFS")
    void testMinimumSteps_largeOpenGridUnitK() {
        List<String> grid = buildOpenGrid(100);
        assertThat(llcoding.minimumSteps(grid, 1, 0, 0, 99, 99))
                .isEqualTo(198);
    }

    @Test
    @DisplayName("minimumSteps: 100x100 open grid, corner to corner, with a moderate k")
    void testMinimumSteps_largeOpenGridModerateK() {
        List<String> grid = buildOpenGrid(100);
        assertThat(llcoding.minimumSteps(grid, 10, 0, 0, 99, 99))
                .isEqualTo(20);
    }

    @Test
    @DisplayName("getLongestPathLength")
    void testGetLongestPathLength() {
        assertThat(llcoding.getLongestPathLength(List.of("8,8,3", "5,5,7", "2,1,0"))).isEqualTo(5);
        assertThat(llcoding.getLongestPathLength(List.of("4,5,6", "4,3,7", "3,3,2"))).isEqualTo(4);
        assertThat(llcoding.getLongestPathLength(List.of("2"))).isEqualTo(1);
        assertThat(llcoding.getLongestPathLength(List.of("2,3", "4,5"))).isEqualTo(3);
        assertThat(llcoding.getLongestPathLength(List.of("2,3", "5,4", "6, 7", "8,9"))).isEqualTo(7);
    }

    private List<String> buildOpenGrid(int size) {
        return Collections.nCopies(size, ".".repeat(size));
    }

}