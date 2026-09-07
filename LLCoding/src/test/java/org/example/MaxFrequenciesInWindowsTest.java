package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MaxFrequenciesInWindows")
class MaxFrequenciesInWindowsTest {

    // Feeds a fixed sequence of numbers one at a time, so tests are deterministic.
    private static MaxFrequenciesInWindows withSequence(int size, int... values) {
        Deque<Integer> source = new ArrayDeque<>();
        for (int v : values) {
            source.addLast(v);
        }
        return new MaxFrequenciesInWindows(size, source::pollFirst);
    }

    // topK returns {value, frequency} pairs; pull out just the values for order-agnostic assertions.
    private static List<Integer> values(List<int[]> topK) {
        return topK.stream().map(pair -> pair[0]).collect(Collectors.toList());
    }

    @Test
    @DisplayName("generate: counts stay accurate while the window is filling up")
    void generateAccumulatesCountsBelowWindowSize() {
        MaxFrequenciesInWindows tracker = withSequence(5, 1, 2, 2, 3, 3, 3);
        for (int i = 0; i < 5; i++) {
            tracker.generate();
        }
        // window: [1, 2, 2, 3, 3] -> counts: 1x1, 2x2, 3x2
        assertThat(values(tracker.topK(3))).containsExactlyInAnyOrder(1, 2, 3);
        assertThat(values(tracker.topK(1))).containsAnyOf(2, 3);
    }

    @Test
    @DisplayName("generate: evicts the oldest element once the window overflows")
    void generateEvictsOldestOnOverflow() {
        MaxFrequenciesInWindows tracker = withSequence(3, 1, 1, 2, 3);
        tracker.generate(); // window: [1]
        tracker.generate(); // window: [1, 1]
        tracker.generate(); // window: [1, 1, 2]
        tracker.generate(); // window: [1, 2, 3]  (oldest "1" evicted, one "1" remains)

        assertThat(values(tracker.topK(3))).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    @DisplayName("generate: fully evicted element is removed from the frequency map")
    void generateRemovesCountWhenElementLeavesWindow() {
        MaxFrequenciesInWindows tracker = withSequence(2, 1, 2, 3);
        tracker.generate(); // window: [1]
        tracker.generate(); // window: [1, 2]
        tracker.generate(); // window: [2, 3]  (1 fully evicted)

        assertThat(values(tracker.topK(10))).containsExactlyInAnyOrder(2, 3);
    }

    @Test
    @DisplayName("topK: returns the k highest-frequency values with correct counts")
    void topKReturnsMostFrequentValuesWithCounts() {
        // 1 appears 4x, 2 appears 3x, 3 appears 2x, 4 appears 1x
        MaxFrequenciesInWindows tracker = withSequence(10, 1, 1, 1, 1, 2, 2, 2, 3, 3, 4);
        for (int i = 0; i < 10; i++) {
            tracker.generate();
        }

        assertThat(values(tracker.topK(2))).containsExactlyInAnyOrder(1, 2);
        assertThat(values(tracker.topK(4))).containsExactlyInAnyOrder(1, 2, 3, 4);

        // verify the frequencies reported alongside the top-2 values
        List<int[]> top2 = tracker.topK(2);
        for (int[] pair : top2) {
            int value = pair[0];
            int freq = pair[1];
            if (value == 1) assertThat(freq).isEqualTo(4);
            if (value == 2) assertThat(freq).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("topK: k <= 0 returns an empty list")
    void topKWithNonPositiveKReturnsEmpty() {
        MaxFrequenciesInWindows tracker = withSequence(5, 1, 2, 3);
        for (int i = 0; i < 3; i++) {
            tracker.generate();
        }

        assertThat(tracker.topK(0)).isEmpty();
        assertThat(tracker.topK(-1)).isEmpty();
    }

    @Test
    @DisplayName("topK: k larger than the number of distinct values returns all of them")
    void topKWithKLargerThanDistinctCountReturnsAll() {
        MaxFrequenciesInWindows tracker = withSequence(5, 1, 2, 3);
        for (int i = 0; i < 3; i++) {
            tracker.generate();
        }

        assertThat(values(tracker.topK(100))).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    @DisplayName("topK: on an empty window returns an empty list")
    void topKOnEmptyWindowReturnsEmpty() {
        MaxFrequenciesInWindows tracker = withSequence(5);
        assertThat(tracker.topK(3)).isEmpty();
    }
}