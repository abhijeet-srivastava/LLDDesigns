package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TreeStringPrefixCount.countPrefixOccurrences")
class TreeStringPrefixCountTest {

    private final TreeStringPrefixCount solver = new TreeStringPrefixCount();

    @Test
    @DisplayName("Star-shaped tree matches the documented worked example")
    void starShapedTreeMatchesWorkedExample() {
        List<Integer> result = solver.countPrefixOccurrences(
                5, List.of("0 1", "1 2", "1 3", "3 4"), "ababa", "aba");

        assertThat(result).containsExactly(3, 3, 2);
    }

    @Test
    @DisplayName("Single-node tree counts one occurrence of its own letter")
    void singleNodeTreeCountsOwnLetter() {
        List<Integer> result = solver.countPrefixOccurrences(1, List.of(), "a", "a");

        assertThat(result).containsExactly(1);
    }

    @Test
    @DisplayName("Prefix whose first character never appears yields all zeros")
    void firstCharacterNeverAppearingYieldsZeros() {
        List<Integer> result = solver.countPrefixOccurrences(
                3, List.of("0 1", "1 2"), "aaa", "bab");

        assertThat(result).containsExactly(0, 0, 0);
    }

    @Test
    @DisplayName("Path-graph tree counts a straight run in both directions")
    void pathGraphTreeCountsStraightRunBothDirections() {
        List<Integer> result = solver.countPrefixOccurrences(
                4, List.of("0 1", "1 2", "2 3"), "abca", "abc");

        assertThat(result).containsExactly(2, 1, 1);
    }

    @Test
    @DisplayName("countVisiblePeople matches the documented worked example")
    void countVisiblePeopleMatchesWorkedExample() {
        int[] result = solver.countVisiblePeople(new int[]{1, 10, 6, 7, 9, 2, 4, 5});

        assertThat(result).containsExactly(1, 7, 3, 3, 6, 4, 4, 4);
    }

    @Test
    @DisplayName("countVisiblePeople for a single person is zero")
    void countVisiblePeopleSinglePersonSeesNoOne() {
        int[] result = solver.countVisiblePeople(new int[]{5});

        assertThat(result).containsExactly(0);
    }

    @Test
    @DisplayName("countVisiblePeople for two people always see each other")
    void countVisiblePeopleTwoPeopleSeeEachOther() {
        int[] result = solver.countVisiblePeople(new int[]{3, 8});

        assertThat(result).containsExactly(1, 1);
    }

    @Test
    @DisplayName("countVisiblePeople with equal heights sees everyone else")
    void countVisiblePeopleEqualHeightsSeeEveryoneElse() {
        int[] result = solver.countVisiblePeople(new int[]{5, 5, 5, 5});

        assertThat(result).containsExactly(3, 3, 3, 3);
    }

    @Test
    @DisplayName("countVisiblePeople with strictly increasing heights sees everyone else")
    void countVisiblePeopleStrictlyIncreasingSeeEveryoneElse() {
        int[] result = solver.countVisiblePeople(new int[]{1, 2, 3, 4});

        assertThat(result).containsExactly(3, 3, 3, 3);
    }

    @Test
    @DisplayName("countVisiblePeople with strictly decreasing heights sees everyone else")
    void countVisiblePeopleStrictlyDecreasingSeeEveryoneElse() {
        int[] result = solver.countVisiblePeople(new int[]{4, 3, 2, 1});

        assertThat(result).containsExactly(3, 3, 3, 3);
    }

    @Test
    @DisplayName("maximumSumSubarray Test case 1")
    void maximumSumSubarrayTest1() {
        long result = solver.maximumSumSubarray(new int[]{1, 2, 3, 2, 1});
        assertThat(result).isEqualTo(9l);
    }
    @Test
    @DisplayName("maximumSumSubarray Test case 2")
    void maximumSumSubarrayTest2() {
        long result = solver.maximumSumSubarray(new int[]{5, -10, 5, 4, -1, 4});
        assertThat(result).isEqualTo(7l);
    }
    @Test
    @DisplayName("maximumSumSubarray Test case 3")
    void maximumSumSubarrayTest3() {
        long result = solver.maximumSumSubarray(new int[]{-3, -2, -3});
        assertThat(result).isEqualTo(-2l);
    }

    @Test
    @DisplayName("maximumSumSubarray Test case 4")
    void maximumSumSubarrayTest4() {
        long result = solver.maximumSumSubarray(new int[]{7,1,2,3});
        assertThat(result).isEqualTo(7l);
    }

    @Test
    @DisplayName("longestConstrainedPath Test case 1")
    void maximumPathInGridTest1() {
        int result = solver.longestConstrainedPath(List.of( "7"));
        assertThat(result).isEqualTo(1);
    }
    @Test
    @DisplayName("longestConstrainedPath Test case 2")
    void longestConstrainedPathTest2() {
        int result = solver.longestConstrainedPath(List.of( "5,4", "3,2"));
        assertThat(result).isEqualTo(4);
    }

    @Test
    @DisplayName("longestConstrainedPath Test case 3")
    void longestConstrainedPathTest3() {
        int result = solver.longestConstrainedPath(List.of( "1,1,1", "2,3,2"));
        assertThat(result).isEqualTo(5);
    }

    @Test
    @DisplayName("possibleIntendedWords Test1")
    void possibleIntendedWordsTest1() {
        List<String> result = solver.possibleIntendedWords("ssuuunn", List.of("sun", "ssun", "sunn", "soon"));

        assertThat(result).containsExactly("sun", "ssun", "sunn");
    }
    @Test
    @DisplayName("possibleIntendedWords Test2")
    void possibleIntendedWordsTest2() {
        List<String> result = solver.possibleIntendedWords("aabbcc", List.of("abc", "aabbcc", "abbc", "abcc", "ac"));

        assertThat(result).containsExactly("abc", "aabbcc", "abbc", "abcc");
    }

    @Test
    @DisplayName("possibleIntendedWords Test3")
    void possibleIntendedWordsTest3() {
        List<String> result = solver.possibleIntendedWords("kkkk", List.of("k", "kk", "kkkk", "kkkkk"));

        assertThat(result).containsExactly("k", "kk", "kkkk");
    }

    @Test
    @DisplayName("compilePackages Test1")
    void compilePackagesTest1() {
        List<String> result = solver.compilePackages(List.of("app,core,ui", "core,util", "ui,util", "util"), 2);

        assertThat(result).containsExactly("util", "core", "ui", "app");
    }

    @Test
    @DisplayName("compilePackages Test2")
    void compilePackagesTest2() {
        List<String> result = solver.compilePackages(List.of("backend,network,storage", "frontend,ui", "network", "storage", "ui"), 2);

        assertThat(result).containsExactly("network", "storage", "backend", "ui", "frontend");
    }

    @Test
    @DisplayName("compilePackages Test3")
    void compilePackagesTest3() {
        List<String> result = solver.compilePackages(List.of("a,b", "b,c", "c,a"), 3);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getWorkingTimeline Test1")
    void getWorkingTimelineTest1() {
        List<String> result = solver.getWorkingTimeline(List.of("Alex,05:00,12:00", "Moris,01:00,11:00", "Rahul,14:00,16:00"));

        assertThat(result).containsExactly("01:00 to 04:59 -> Moris", "05:00 to 11:00 -> Alex, Moris", "11:01 to 12:00 -> Alex", "14:00 to 16:00 -> Rahul");
    }

    @Test
    @DisplayName("getWorkingTimeline Test2")
    void getWorkingTimelineTest2() {
        List<String> result = solver.getWorkingTimeline(List.of("Aman,09:00,10:30", "Bhavna,09:00,11:00", "Chirag,10:30,12:00"));

        assertThat(result).containsExactly("09:00 to 10:29 -> Aman, Bhavna", "10:30 to 10:30 -> Aman, Bhavna, Chirag", "10:31 to 11:00 -> Bhavna, Chirag", "11:01 to 12:00 -> Chirag");
    }

    @Test
    @DisplayName("getWorkingTimeline Test3")
    void getWorkingTimelineTest3() {
        List<String> result = solver.getWorkingTimeline(List.of("Aman,09:00,10:30",
                "Bhavna,09:00,11:00",
                "Chirag,10:30,12:00",
                "Avantika, 09:45, 10:15",
                "Chhaya, 06:10, 11:30"));

        assertThat(result).containsExactly("06:10 to 08:59 -> Chhaya",
                "09:00 to 09:44 -> Aman, Bhavna, Chhaya",
                "09:45 to 10:15 -> Aman, Avantika, Bhavna, Chhaya",
                "10:16 to 10:29 -> Aman, Bhavna, Chhaya",
                "10:30 to 10:30 -> Aman, Bhavna, Chhaya, Chirag",
                "10:31 to 11:00 -> Bhavna, Chhaya, Chirag",
                "11:01 to 11:30 -> Chhaya, Chirag",
                "11:31 to 12:00 -> Chirag");
    }

    @Test
    @DisplayName("getDaysWhenAtLeastKPeopleAreFree Test1")
    void getDaysWhenAtLeastKPeopleAreFreeTest1() {
        List<Integer> result = solver.getDaysWhenAtLeastKPeopleAreFree(List.of("1,2,3", "2,5,6"), 6, 2);

        assertThat(result).containsExactly(1,4);
    }
    @Test
    @DisplayName("getDaysWhenAtLeastKPeopleAreFree Test2")
    void getDaysWhenAtLeastKPeopleAreFreeTest2() {
        List<Integer> result = solver.getDaysWhenAtLeastKPeopleAreFree(List.of("1,2,3", "2,5,6"), 6, 1);

        assertThat(result).containsExactly(1,2,3,4,5,6);
    }
    @Test
    @DisplayName("getDaysWhenAtLeastKPeopleAreFree Test3")
    void getDaysWhenAtLeastKPeopleAreFreeTest3() {
        List<Integer> result = solver.getDaysWhenAtLeastKPeopleAreFree(List.of("1,1,2", "1,5,5", "2,2,3", "3,4,4"), 5, 3);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("minCpusNeeded Test1")
    void minCpusNeededTest1() {
        int result = solver.minCpusNeeded(List.of(0,0, 0, 10000), 5);

        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("minCpusNeeded Test2")
    void minCpusNeededTest2() {
        int result = solver.minCpusNeeded(List.of(0,0, 0), 5);

        assertThat(result).isEqualTo(3);
    }

    @Test
    @DisplayName("minCpusNeeded Test3")
    void minCpusNeededTest3() {
        int result = solver.minCpusNeeded(List.of(0,5, 10), 5);

        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("minCpusNeeded Test4")
    void minCpusNeededTest4() {
        int result = solver.minCpusNeeded(List.of(0,4, 8), 10);

        assertThat(result).isEqualTo(3);
    }

    @Test
    @DisplayName("computeOverallErrorMetric Test1")
    void computeOverallErrorMetricTest1() {
        double result = solver.computeOverallErrorMetric(List.of("0,0,0", "10,10,0"), List.of("5,6,0", "10,10,0"));

        assertThat(result).isEqualTo(1.0d);
    }

    @Test
    @DisplayName("computeOverallErrorMetric Test2")
    void computeOverallErrorMetricTest2() {
        double result = solver.computeOverallErrorMetric(List.of("0,0,0", "10,10,10"), List.of("0,0,0", "2,2,3", "8,8,8"));

        assertThat(result).isEqualTo(1.0d);
    }

    @Test
    @DisplayName("computeOverallErrorMetric Test3")
    void computeOverallErrorMetricTest3() {
        double result = solver.computeOverallErrorMetric(List.of("10,0,0", "20,10,0"), List.of("5,100,100", "15,6,0", "25,1,1"));

        assertThat(result).isEqualTo(1.0d);
    }

    @Test
    @DisplayName("computeOverallErrorMetric brackets correctly across three checkpoints (regression for inner-loop off-by-one)")
    void computeOverallErrorMetricBracketsAcrossMultipleCheckpoints() {
        double result = solver.computeOverallErrorMetric(
                List.of("0,0,0", "10,10,0", "20,20,0"), List.of("19,19,1"));

        assertThat(result).isEqualTo(1.0d);
    }

    @Test
    @DisplayName("computeOverallErrorMetric returns zero for an empty checkpoints list")
    void computeOverallErrorMetricEmptyCheckpointsReturnsZero() {
        double result = solver.computeOverallErrorMetric(List.of(), List.of("5,1,1"));

        assertThat(result).isEqualTo(0.0d);
    }
}
