package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Main grid-path algorithms")
class MainTest {

    private final Main main = new Main();
    private static final int[][] DIRS = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

    @Nested
    @DisplayName("minimumElevationGain")
    class MinimumElevationGain {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.example.MainTest#elevationGainCases")
        @DisplayName("Computes the expected minimum elevation gain")
        void computesExpectedMinimumElevationGain(String description, int[][] heights, int expected) {
            assertThat(main.minimumElevationGain(heights))
                    .as(description)
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Non-square grid matches brute-force enumeration of all simple paths")
        void nonSquareGridMatchesBruteForce() {
            // Arrange
            int[][] heights = {{1, 3, 2}, {4, 1, 5}};

            // Act
            int actual = main.minimumElevationGain(heights);

            // Assert
            assertThat(actual).isEqualTo(bruteForceMinElevationGain(heights));
        }

        @Test
        @DisplayName("Matches brute-force enumeration across many random small grids")
        void matchesBruteForceAcrossRandomGrids() {
            // Arrange
            Random random = new Random(42);

            for (int trial = 0; trial < 30; trial++) {
                int rows = 1 + random.nextInt(3);
                int cols = 1 + random.nextInt(3);
                int[][] heights = randomGrid(random, rows, cols, -5, 10);

                // Act
                int actual = main.minimumElevationGain(heights);

                // Assert
                assertThat(actual)
                        .as("grid %s", Arrays.deepToString(heights))
                        .isEqualTo(bruteForceMinElevationGain(heights));
            }
        }

        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("Matches an independent Bellman-Ford oracle on a large 100x100 grid")
        void matchesBellmanFordOracleOnLargeGrid() {
            // Arrange
            int[][] heights = randomGrid(new Random(101), 100, 100, 0, 8);

            // Act
            int actual = main.minimumElevationGain(heights);

            // Assert
            assertThat(actual).isEqualTo(bellmanFordMinElevationGain(heights));
        }

        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("Matches an independent Bellman-Ford oracle across several large random grids, including negative heights")
        void matchesBellmanFordOracleAcrossLargeRandomGrids() {
            // Arrange
            Random random = new Random(103);

            for (int trial = 0; trial < 3; trial++) {
                int rows = 60 + random.nextInt(41); // 60-100
                int cols = 60 + random.nextInt(41); // 60-100
                int[][] heights = randomGrid(random, rows, cols, -10, 10);

                // Act
                int actual = main.minimumElevationGain(heights);

                // Assert
                assertThat(actual)
                        .as("grid %dx%d", rows, cols)
                        .isEqualTo(bellmanFordMinElevationGain(heights));
            }
        }
    }

    @Nested
    @DisplayName("minimumEffortPathDijkstra")
    class MinimumEffortPathDijkstra {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.example.MainTest#effortPathCases")
        @DisplayName("Computes the expected minimum effort")
        void computesExpectedMinimumEffort(String description, int[][] heights, int expected) {
            assertThat(main.minimumEffortPathDijkstra(heights))
                    .as(description)
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Agrees with the independently-implemented DFS/binary-search solution across random grids")
        void matchesDfsOracleAcrossRandomGrids() {
            // Arrange
            Random random = new Random(7);

            for (int trial = 0; trial < 30; trial++) {
                int rows = 1 + random.nextInt(4);
                int cols = 1 + random.nextInt(4);
                int[][] heights = randomGrid(random, rows, cols, -5, 10);

                // Act
                int actual = main.minimumEffortPathDijkstra(heights);

                // Assert
                assertThat(actual)
                        .as("grid %s", Arrays.deepToString(heights))
                        .isEqualTo(main.minimumEffortPathDfs(heights));
            }
        }

        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("Agrees with the DFS/binary-search oracle on large 80-100 sized grids")
        void matchesDfsOracleOnLargeGrids() {
            // Arrange
            Random random = new Random(107);

            for (int trial = 0; trial < 5; trial++) {
                int rows = 80 + random.nextInt(21); // 80-100
                int cols = 80 + random.nextInt(21); // 80-100
                int[][] heights = randomGrid(random, rows, cols, 0, 100);

                // Act
                int actual = main.minimumEffortPathDijkstra(heights);

                // Assert
                assertThat(actual)
                        .as("grid %dx%d", rows, cols)
                        .isEqualTo(main.minimumEffortPathDfs(heights));
            }
        }
    }

    @Nested
    @DisplayName("minimumEffortPathDfs")
    class MinimumEffortPathDfs {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.example.MainTest#effortPathCases")
        @DisplayName("Computes the expected minimum effort")
        void computesExpectedMinimumEffort(String description, int[][] heights, int expected) {
            assertThat(main.minimumEffortPathDfs(heights))
                    .as(description)
                    .isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("minimumEffortPath")
    class MinimumEffortPath {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.example.MainTest#effortPathCases")
        @DisplayName("Computes the expected minimum effort")
        void computesExpectedMinimumEffort(String description, int[][] heights, int expected) {
            assertThat(main.minimumEffortPath(heights))
                    .as(description)
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Agrees with the Dijkstra solution across random grids")
        void matchesDijkstraAcrossRandomGrids() {
            // Arrange
            Random random = new Random(11);

            for (int trial = 0; trial < 30; trial++) {
                int rows = 1 + random.nextInt(4);
                int cols = 1 + random.nextInt(4);
                int[][] heights = randomGrid(random, rows, cols, -5, 10);

                // Act
                int actual = main.minimumEffortPath(heights);

                // Assert
                assertThat(actual)
                        .as("grid %s", Arrays.deepToString(heights))
                        .isEqualTo(main.minimumEffortPathDijkstra(heights));
            }
        }

        @Test
        @DisplayName("Agrees with the DFS/binary-search solution across random grids")
        void matchesDfsAcrossRandomGrids() {
            // Arrange
            Random random = new Random(13);

            for (int trial = 0; trial < 30; trial++) {
                int rows = 1 + random.nextInt(4);
                int cols = 1 + random.nextInt(4);
                int[][] heights = randomGrid(random, rows, cols, -5, 10);

                // Act
                int actual = main.minimumEffortPath(heights);

                // Assert
                assertThat(actual)
                        .as("grid %s", Arrays.deepToString(heights))
                        .isEqualTo(main.minimumEffortPathDfs(heights));
            }
        }

        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("All three independent implementations agree on large 80-100 sized grids")
        void allImplementationsAgreeOnLargeGrids() {
            // Arrange
            Random random = new Random(113);

            for (int trial = 0; trial < 5; trial++) {
                int rows = 80 + random.nextInt(21); // 80-100
                int cols = 80 + random.nextInt(21); // 80-100
                int[][] heights = randomGrid(random, rows, cols, 0, 200);

                // Act
                int viaUnionFind = main.minimumEffortPath(heights);
                int viaDijkstra = main.minimumEffortPathDijkstra(heights);
                int viaDfs = main.minimumEffortPathDfs(heights);

                // Assert
                assertThat(viaUnionFind)
                        .as("grid %dx%d", rows, cols)
                        .isEqualTo(viaDijkstra)
                        .isEqualTo(viaDfs);
            }
        }
    }

    @Nested
    @DisplayName("findPath")
    class FindPath {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.example.MainTest#findPathCases")
        @DisplayName("Computes the expected minimum-hop path")
        void computesExpectedPath(String description, int m, int n, int[] start, int[] target, String expected) {
            assertThat(main.findPath(m, n, start, target))
                    .as(description)
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Path length matches the Manhattan-distance shortest-hop count across random start/target pairs")
        void pathLengthMatchesManhattanDistanceAcrossRandomPairs() {
            // Arrange
            Random random = new Random(17);

            for (int trial = 0; trial < 30; trial++) {
                int m = 1 + random.nextInt(6);
                int n = 1 + random.nextInt(6);
                int[] start = {random.nextInt(m), random.nextInt(n)};
                int[] target = {random.nextInt(m), random.nextInt(n)};

                // Act
                String path = main.findPath(m, n, start, target);
                List<int[]> cells = parsePath(path);

                // Assert
                int expectedHops = Math.abs(target[0] - start[0]) + Math.abs(target[1] - start[1]);
                assertThat(cells.size() - 1)
                        .as("hop count for start=%s target=%s on a %dx%d grid", Arrays.toString(start), Arrays.toString(target), m, n)
                        .isEqualTo(expectedHops);
            }
        }

        @Test
        @DisplayName("Path is well-formed: starts at start, ends at target, single-step moves, no repeated cells")
        void pathIsWellFormedAcrossRandomPairs() {
            // Arrange
            Random random = new Random(19);

            for (int trial = 0; trial < 30; trial++) {
                int m = 1 + random.nextInt(6);
                int n = 1 + random.nextInt(6);
                int[] start = {random.nextInt(m), random.nextInt(n)};
                int[] target = {random.nextInt(m), random.nextInt(n)};

                // Act
                String path = main.findPath(m, n, start, target);
                List<int[]> cells = parsePath(path);

                // Assert
                assertThat(cells.get(0))
                        .as("first cell should be start")
                        .isEqualTo(start);
                assertThat(cells.get(cells.size() - 1))
                        .as("last cell should be target")
                        .isEqualTo(target);

                for (int i = 1; i < cells.size(); i++) {
                    int[] prev = cells.get(i - 1);
                    int[] curr = cells.get(i);
                    int stepDistance = Math.abs(curr[0] - prev[0]) + Math.abs(curr[1] - prev[1]);
                    assertThat(stepDistance)
                            .as("consecutive cells %s -> %s should be a single grid step", Arrays.toString(prev), Arrays.toString(curr))
                            .isEqualTo(1);
                }

                long distinctCells = cells.stream().map(Arrays::toString).distinct().count();
                assertThat(distinctCells)
                        .as("path should not revisit any cell")
                        .isEqualTo(cells.size());
            }
        }

        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("Hop count matches Manhattan distance on a 100x100 grid across all corner-to-corner pairs")
        void hopCountMatchesManhattanDistanceOnLargeGridCorners() {
            // Arrange
            int m = 100, n = 100;
            int[][] corners = {{0, 0}, {0, n - 1}, {m - 1, 0}, {m - 1, n - 1}};

            for (int[] start : corners) {
                for (int[] target : corners) {
                    if (Arrays.equals(start, target)) {
                        continue;
                    }

                    // Act
                    String path = main.findPath(m, n, start, target);
                    List<int[]> cells = parsePath(path);

                    // Assert
                    int expectedHops = Math.abs(target[0] - start[0]) + Math.abs(target[1] - start[1]);
                    assertThat(cells.size() - 1)
                            .as("start %s target %s", Arrays.toString(start), Arrays.toString(target))
                            .isEqualTo(expectedHops);
                }
            }
        }

        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("Path remains well-formed on a 100x100 grid across random start/target pairs")
        void pathIsWellFormedOnLargeGrid() {
            // Arrange
            Random random = new Random(127);
            int m = 100, n = 100;

            for (int trial = 0; trial < 10; trial++) {
                int[] start = {random.nextInt(m), random.nextInt(n)};
                int[] target = {random.nextInt(m), random.nextInt(n)};

                // Act
                String path = main.findPath(m, n, start, target);
                List<int[]> cells = parsePath(path);

                // Assert
                assertThat(cells.get(0)).as("first cell should be start").isEqualTo(start);
                assertThat(cells.get(cells.size() - 1)).as("last cell should be target").isEqualTo(target);

                for (int i = 1; i < cells.size(); i++) {
                    int[] prev = cells.get(i - 1);
                    int[] curr = cells.get(i);
                    int stepDistance = Math.abs(curr[0] - prev[0]) + Math.abs(curr[1] - prev[1]);
                    assertThat(stepDistance)
                            .as("consecutive cells %s -> %s should be a single grid step", Arrays.toString(prev), Arrays.toString(curr))
                            .isEqualTo(1);
                }

                long distinctCells = cells.stream().map(Arrays::toString).distinct().count();
                assertThat(distinctCells)
                        .as("path should not revisit any cell")
                        .isEqualTo(cells.size());
            }
        }
    }

    @Nested
    @DisplayName("canReachDestination")
    class CanReachDestination {

        @Test
        @DisplayName("Unknown source or destination router returns false")
        void unknownRouterReturnsFalse() {
            List<String> routers = List.of("A,0,0", "B,1,0");

            assertThat(main.canReachDestination(routers, "Z", "B", 5)).isFalse();
            assertThat(main.canReachDestination(routers, "A", "Z", 5)).isFalse();
        }

        @Test
        @DisplayName("Source equal to destination is trivially reachable")
        void sourceEqualsDestinationIsReachable() {
            List<String> routers = List.of("A,0,0", "B,1,0");

            assertThat(main.canReachDestination(routers, "A", "A", 5)).isTrue();
        }

        @Test
        @DisplayName("Direct single-hop reachability requires distance <= range, not distance == range")
        void directHopRequiresDistanceLessThanOrEqualToRange() {
            // A and B are 3 units apart; a range comfortably larger than 3 (not close to it)
            // must still connect them -- distance-<=-range, not distance-approximately-equal-range.
            List<String> routers = List.of("A,0,0", "B,3,0");

            assertThat(main.canReachDestination(routers, "A", "B", 10)).isTrue();
        }

        @Test
        @DisplayName("Destination out of range of every router is unreachable")
        void outOfRangeIsUnreachable() {
            List<String> routers = List.of("A,0,0", "B,100,100");

            assertThat(main.canReachDestination(routers, "A", "B", 5)).isFalse();
        }

        @Test
        @DisplayName("Multi-hop path is reachable when each hop is within range")
        void multiHopPathIsReachable() {
            // A -> B -> C, each hop distance 1, well within range; A to C directly is out of range.
            List<String> routers = List.of("A,0,0", "B,1,0", "C,2,0");

            assertThat(main.canReachDestination(routers, "A", "C", 1)).isTrue();
        }

        @Test
        @DisplayName("Two messages tying for a router's earliest arrival time collide and are discarded")
        void tiedEarliestArrivalsCollideAndAreDiscarded() {
            // A -> B (dist 3) -> D (dist 3) totals 6, and A -> C (dist 3) -> D (dist 3) also
            // totals 6: D receives two messages at the same earliest arrival time (6) and must
            // discard both, never broadcasting. A -> D directly is out of range (~4.24 > 4), so
            // this tie is D's *only* route in, and E (reachable only via D) becomes unreachable.
            List<String> routers = List.of(
                    "A,0,0",
                    "B,3,0",
                    "C,0,3",
                    "D,3,3",
                    "E,3,6");

            assertThat(main.canReachDestination(routers, "A", "D", 4)).isFalse();
            assertThat(main.canReachDestination(routers, "A", "E", 4)).isFalse();
        }

        @Test
        @DisplayName("A single unambiguous earliest arrival still broadcasts normally (no false-positive collision)")
        void uniqueEarliestArrivalStillBroadcasts() {
            // Direct A -> D (dist ~3.16) is strictly shorter than the detour A -> B -> D
            // (1 + 3 = 4), so D's earliest arrival is unique (via the direct hop); the slower
            // detour message simply arrives too late and is ignored, not treated as a collision.
            List<String> routers = List.of(
                    "A,0,0",
                    "B,1,0",
                    "D,1,3");

            assertThat(main.canReachDestination(routers, "A", "D", 5)).isTrue();
        }
    }

    @Nested
    @DisplayName("canReachDestinationBFS")
    class CanReachDestinationBFS {

        @Test
        @DisplayName("Unknown source or destination router returns false")
        void unknownRouterReturnsFalse() {
            List<String> routers = List.of("A,0,0", "B,1,0");

            assertThat(main.canReachDestinationBFS(routers, "Z", "B", 5)).isFalse();
            assertThat(main.canReachDestinationBFS(routers, "A", "Z", 5)).isFalse();
        }

        @Test
        @DisplayName("Source equal to destination is trivially reachable")
        void sourceEqualsDestinationIsReachable() {
            List<String> routers = List.of("A,0,0", "B,1,0");

            assertThat(main.canReachDestinationBFS(routers, "A", "A", 5)).isTrue();
        }

        @Test
        @DisplayName("An isolated source (no router within range) returns false without throwing")
        void isolatedSourceReturnsFalseWithoutThrowing() {
            // Nodes are marked visited on enqueue and neighbor lookups use getOrDefault, so
            // an isolated source (no edges at all) returns false cleanly instead of throwing.
            List<String> routers = List.of("A,0,0", "B,100,100");

            assertThat(main.canReachDestinationBFS(routers, "A", "B", 5)).isFalse();
        }

        @Test
        @DisplayName("A direct hop at distance ~1.0 is incorrectly discarded even though no collision occurred")
        void directHopNearUnitDistanceIsIncorrectlyDiscarded() {
            List<String> routers = List.of("A,0,0", "B,1,0");
            assertThat(main.canReachDestinationBFS(routers, "A", "B", 5)).isTrue();
        }

        @Test
        @DisplayName("A direct hop at a distance away from 1.0 is (coincidentally) unaffected by the bug")
        void directHopAwayFromUnitDistanceWorks() {
            List<String> routers = List.of("A,0,0", "B,3,0");

            assertThat(main.canReachDestinationBFS(routers, "A", "B", 5)).isTrue();
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.example.MainTest#canReachDestinationBfsScenarios")
        @DisplayName("Agrees with the validated canReachDestination implementation")
        void agreesWithValidatedImplementation(String description, List<String> routers, String source,
                                                String destination, double range, boolean expected) {
            assertThat(main.canReachDestinationBFS(routers, source, destination, range))
                    .as(description)
                    .isEqualTo(expected);
        }
    }

    static Stream<Arguments> canReachDestinationBfsScenarios() {
        // canReachDestinationBFS marks nodes visited on enqueue, so cyclic graphs (e.g. the
        // "tied earliest arrivals" scenarios below, whose graph contains a 4-cycle) are safe
        // to include here without risking non-termination.
        return Stream.of(
                Arguments.of("unknown source router",
                        List.of("A,0,0", "B,1,0"), "Z", "B", 5.0, false),
                Arguments.of("unknown destination router",
                        List.of("A,0,0", "B,1,0"), "A", "Z", 5.0, false),
                Arguments.of("source equals destination",
                        List.of("A,0,0", "B,1,0"), "A", "A", 5.0, true),
                Arguments.of("direct hop within range (not merely close to range)",
                        List.of("A,0,0", "B,3,0"), "A", "B", 10.0, true),
                Arguments.of("destination out of range of every reachable router",
                        List.of("A,0,0", "B,1,0", "C,100,100"), "A", "C", 5.0, false),
                Arguments.of("multi-hop path is reachable",
                        List.of("A,0,0", "B,1,0", "C,2,0"), "A", "C", 1.0, true),
                Arguments.of("tied earliest arrivals collide and are discarded",
                        List.of("A,0,0", "B,3,0", "C,0,3", "D,3,3", "E,3,6"), "A", "D", 4.0, false),
                Arguments.of("tied earliest arrivals collide -- downstream router also unreachable",
                        List.of("A,0,0", "B,3,0", "C,0,3", "D,3,3", "E,3,6"), "A", "E", 4.0, false),
                Arguments.of("unique earliest arrival still broadcasts",
                        List.of("A,0,0", "B,1,0", "D,1,3"), "A", "D", 5.0, true)
        );
    }

    @Nested
    @DisplayName("maximumSafenessFactor")
    class MaximumSafenessFactor {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.example.MainTest#safenessFactorCases")
        @DisplayName("Computes the expected maximum safeness factor")
        void computesExpectedSafenessFactor(String description, int m, int n, int[] start, int[] target,
                                             int[][] cats, int expected) {
            assertThat(main.maximumSafenessFactor(m, n, start, target, cats))
                    .as(description)
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Matches an independent brute-force oracle across random larger grids with multiple cats and corner start/target pairs")
        void matchesBruteForceAcrossRandomGridsWithCornerEndpoints() {
            // Arrange
            Random random = new Random(29);

            for (int trial = 0; trial < 40; trial++) {
                int m = 3 + random.nextInt(4);  // m in [3, 6]
                int n = 4 + random.nextInt(4);  // n in [4, 7]
                int[][] corners = {{0, 0}, {0, n - 1}, {m - 1, 0}, {m - 1, n - 1}};
                int[] start = corners[random.nextInt(corners.length)];
                int[] target;
                do {
                    target = corners[random.nextInt(corners.length)];
                } while (Arrays.equals(target, start));

                int catCount = 2 + random.nextInt(3); // 2 to 4 cats
                int[][] cats = new int[catCount][];
                for (int i = 0; i < catCount; i++) {
                    cats[i] = new int[]{random.nextInt(m), random.nextInt(n)};
                }

                // Act
                int actual = main.maximumSafenessFactor(m, n, start, target, cats);

                // Assert
                assertThat(actual)
                        .as("grid %dx%d, start %s, target %s, cats %s",
                                m, n, Arrays.toString(start), Arrays.toString(target), Arrays.deepToString(cats))
                        .isEqualTo(bruteForceMaxSafeness(m, n, start, target, cats));
            }
        }
    }

    @Nested
    @DisplayName("calculateExpression")
    class CalculateExpression {

        @Test
        @DisplayName("Minimum expression with add operator")
        void minExpressionWithAddOp() {
            int result  = main.calculateExpression(" 2 + 3 ");
            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("Minimum expression with sub operator")
        void minExpressionWithSubOp() {
            int result  = main.calculateExpression(" 28 - 21 ");
            assertThat(result).isEqualTo(7);
        }

        @Test
        @DisplayName("Minimum expression with mul operator")
        void minExpressionWithMulOp() {
            int result  = main.calculateExpression(" 15 * 4 ");
            assertThat(result).isEqualTo(60);
        }

        @Test
        @DisplayName("Minimum expression with mul operator and uniary")
        void minExpressionWithMulOpAndUni() {
            int result  = main.calculateExpression(" -15 * 4 ");
            assertThat(result).isEqualTo(-60);
        }

        @Test
        @DisplayName("Minimum expression with div operator")
        void minExpressionWithDinOp() {
            int result  = main.calculateExpression(" 15 * 3 ");
            assertThat(result).isEqualTo(45);
        }

        @Test
        @DisplayName("Minimum expression with div operator and uniary")
        void minExpressionWithDivOpAndUni() {
            int result  = main.calculateExpression(" -15 * 3 ");
            assertThat(result).isEqualTo(-45);
        }

        @Test
        @DisplayName("Minimum expression with add and mul operator")
        void minExpressionWithAddAndMulOp() {
            int result  = main.calculateExpression(" 2 + 3 * 4");
            assertThat(result).isEqualTo(14);
        }

        @Test
        @DisplayName("Minimum expression with add and mul operator, braces got precedence")
        void minExpressionWithAddAndMulBracesGotPrecedence() {
            int result  = main.calculateExpression(" (2 + 3) * 4");
            assertThat(result).isEqualTo(20);
        }
    }

    static Stream<Arguments> safenessFactorCases() {
        return Stream.of(
                Arguments.of("start cell has a cat -> zero safeness",
                        3, 3, new int[]{0, 0}, new int[]{2, 2}, new int[][]{{0, 0}}, 0),
                Arguments.of("LeetCode 2812 example 2",
                        3, 3, new int[]{0, 0}, new int[]{2, 2}, new int[][]{{0, 2}}, 2),
                Arguments.of("LeetCode 2812 example 3",
                        4, 4, new int[]{0, 0}, new int[]{3, 3}, new int[][]{{0, 3}, {3, 0}}, 2),
                Arguments.of("3x5 grid, two cats, corner-to-corner: reachability forces a lower safeness than the corner distances alone suggest",
                        3, 5, new int[]{0, 0}, new int[]{2, 4}, new int[][]{{1, 2}, {0, 4}}, 1),
                Arguments.of("small: 3x4 grid, single interior cat",
                        3, 4, new int[]{0, 0}, new int[]{2, 3}, new int[][]{{1, 1}}, 1),
                Arguments.of("small: 4x4 grid, two cats, opposite-corner start/target",
                        4, 4, new int[]{0, 3}, new int[]{3, 0}, new int[][]{{1, 1}, {2, 2}}, 1),
                Arguments.of("small: 4x5 grid, single centered cat",
                        4, 5, new int[]{0, 0}, new int[]{3, 4}, new int[][]{{2, 2}}, 2),
                Arguments.of("small: 3x5 grid, two symmetric cats",
                        3, 5, new int[]{0, 0}, new int[]{2, 4}, new int[][]{{0, 2}, {2, 2}}, 1),
                Arguments.of("large: 100x100 grid, 8 scattered cats, corner-to-corner",
                        100, 100, new int[]{0, 0}, new int[]{99, 99},
                        new int[][]{{20, 20}, {20, 80}, {80, 20}, {80, 80}, {50, 50}, {10, 90}, {90, 10}, {45, 55}}, 19),
                Arguments.of("large: 100x100 grid, 7 scattered cats, anti-diagonal corners",
                        100, 100, new int[]{0, 99}, new int[]{99, 0},
                        new int[][]{{15, 15}, {15, 85}, {85, 15}, {85, 85}, {50, 50}, {30, 70}, {70, 30}}, 15)
        );
    }

    /**
     * Independent oracle: safeness of a cell is its true shortest-path distance to the
     * nearest cat, which on an open (obstacle-free) grid equals the minimum Manhattan
     * distance to any cat. The answer is the largest threshold T for which start and
     * target are both at safeness >= T and are connected using only cells at safeness >= T.
     */
    private static int bruteForceMaxSafeness(int m, int n, int[] start, int[] target, int[][] cats) {
        for (int[] cat : cats) {
            if (Arrays.equals(cat, start) || Arrays.equals(cat, target)) {
                return 0;
            }
        }
        int[][] safeness = new int[m][n];
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                int best = Integer.MAX_VALUE;
                for (int[] cat : cats) {
                    best = Math.min(best, Math.abs(r - cat[0]) + Math.abs(c - cat[1]));
                }
                safeness[r][c] = best;
            }
        }
        int lo = 0, hi = m * n, ans = 0;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (oracleExistsPathAtSafeness(mid, start, target, safeness)) {
                ans = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return ans;
    }

    private static boolean oracleExistsPathAtSafeness(int threshold, int[] start, int[] target, int[][] safeness) {
        int m = safeness.length, n = safeness[0].length;
        if (safeness[start[0]][start[1]] < threshold || safeness[target[0]][target[1]] < threshold) {
            return false;
        }
        boolean[][] visited = new boolean[m][n];
        java.util.Deque<int[]> stack = new java.util.ArrayDeque<>();
        stack.push(start);
        visited[start[0]][start[1]] = true;
        while (!stack.isEmpty()) {
            int[] cur = stack.pop();
            if (cur[0] == target[0] && cur[1] == target[1]) {
                return true;
            }
            for (int[] dir : DIRS) {
                int r = cur[0] + dir[0], c = cur[1] + dir[1];
                if (r < 0 || r >= m || c < 0 || c >= n || visited[r][c] || safeness[r][c] < threshold) {
                    continue;
                }
                visited[r][c] = true;
                stack.push(new int[]{r, c});
            }
        }
        return false;
    }

    static Stream<Arguments> elevationGainCases() {
        return Stream.of(
                Arguments.of("single cell grid", new int[][]{{5}}, 0),
                Arguments.of("single row, strictly increasing, sums every uphill step",
                        new int[][]{{1, 3, 6, 10}}, 9),
                Arguments.of("single row, strictly decreasing, is entirely free",
                        new int[][]{{10, 7, 3, 1}}, 0),
                Arguments.of("single column accumulates only the uphill segments",
                        new int[][]{{1}, {5}, {2}, {8}}, 10),
                Arguments.of("flat grid of any shape",
                        new int[][]{{5, 5, 5, 5}, {5, 5, 5, 5}}, 0),
                // Direct row-then-down route (1 -> 100 -> 3) costs 99; down-then-right (1 -> 2 -> 3) costs 2.
                Arguments.of("picks the genuinely cheaper of two competing routes",
                        new int[][]{{1, 100}, {2, 3}}, 2),
                Arguments.of("ties between two equally-costed routes resolve to the shared minimum",
                        new int[][]{{1, 3}, {3, 5}}, 4),
                Arguments.of("negative-valued heights are handled like any other elevation",
                        new int[][]{{-5, -2, -10, 0}}, 13)
        );
    }

    static Stream<Arguments> effortPathCases() {
        return Stream.of(
                Arguments.of("single cell grid", new int[][]{{5}}, 0),
                Arguments.of("flat grid", new int[][]{{5, 5, 5}, {5, 5, 5}, {5, 5, 5}}, 0),
                Arguments.of("known example grid",
                        new int[][]{{1, 2, 2}, {3, 8, 2}, {5, 3, 5}}, 2)
        );
    }

    static Stream<Arguments> findPathCases() {
        return Stream.of(
                Arguments.of("start equals target returns the single cell", 3, 3, new int[]{1, 1}, new int[]{1, 1}, "(1, 1)"),
                Arguments.of("adjacent cells to the right", 2, 2, new int[]{0, 0}, new int[]{0, 1}, "(0, 0) -> (0, 1)"),
                Arguments.of("adjacent cells below", 2, 2, new int[]{0, 0}, new int[]{1, 0}, "(0, 0) -> (1, 0)"),
                Arguments.of("single row requires a straight walk", 1, 4, new int[]{0, 0}, new int[]{0, 3},
                        "(0, 0) -> (0, 1) -> (0, 2) -> (0, 3)"),
                Arguments.of("single column requires a straight walk", 4, 1, new int[]{0, 0}, new int[]{3, 0},
                        "(0, 0) -> (1, 0) -> (2, 0) -> (3, 0)")
        );
    }

    private static List<int[]> parsePath(String path) {
        List<int[]> cells = new ArrayList<>();
        for (String token : path.split(" -> ")) {
            String cleaned = token.replace("(", "").replace(")", "").trim();
            String[] parts = cleaned.split(",\\s*");
            cells.add(new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])});
        }
        return cells;
    }

    private static int[][] randomGrid(Random random, int rows, int cols, int minVal, int maxVal) {
        int[][] grid = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = minVal + random.nextInt(maxVal - minVal + 1);
            }
        }
        return grid;
    }

    /**
     * Exhaustively enumerates every simple path from (0,0) to (m-1,n-1) and returns
     * the true minimum total uphill gain. Only used as a test oracle for small grids.
     */
    private static int bruteForceMinElevationGain(int[][] heights) {
        int m = heights.length, n = heights[0].length;
        boolean[][] visited = new boolean[m][n];
        visited[0][0] = true;
        int[] best = {Integer.MAX_VALUE};
        bruteForceWalk(0, 0, 0, heights, visited, best);
        return best[0];
    }

    private static void bruteForceWalk(int row, int col, int gainSoFar, int[][] heights,
                                        boolean[][] visited, int[] best) {
        int m = heights.length, n = heights[0].length;
        if (row == m - 1 && col == n - 1) {
            best[0] = Math.min(best[0], gainSoFar);
            return;
        }
        if (gainSoFar >= best[0]) {
            return;
        }
        for (int[] dir : DIRS) {
            int r = row + dir[0], c = col + dir[1];
            if (r < 0 || r >= m || c < 0 || c >= n || visited[r][c]) {
                continue;
            }
            visited[r][c] = true;
            int step = Math.max(0, heights[r][c] - heights[row][col]);
            bruteForceWalk(r, c, gainSoFar + step, heights, visited, best);
            visited[r][c] = false;
        }
    }

    /**
     * Independent oracle for large grids where exhaustive path enumeration is infeasible:
     * plain Bellman-Ford (repeated edge relaxation to a fixed point), rather than Dijkstra's
     * priority-queue ordering, so it does not share the production implementation's algorithm.
     */
    private static int bellmanFordMinElevationGain(int[][] heights) {
        int m = heights.length, n = heights[0].length;
        int[][] gainTo = new int[m][n];
        for (int[] row : gainTo) Arrays.fill(row, Integer.MAX_VALUE);
        gainTo[0][0] = 0;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int r = 0; r < m; r++) {
                for (int c = 0; c < n; c++) {
                    if (gainTo[r][c] == Integer.MAX_VALUE) {
                        continue;
                    }
                    for (int[] dir : DIRS) {
                        int nr = r + dir[0], nc = c + dir[1];
                        if (nr < 0 || nr >= m || nc < 0 || nc >= n) {
                            continue;
                        }
                        int step = Math.max(0, heights[nr][nc] - heights[r][c]);
                        int candidate = gainTo[r][c] + step;
                        if (candidate < gainTo[nr][nc]) {
                            gainTo[nr][nc] = candidate;
                            changed = true;
                        }
                    }
                }
            }
        }
        return gainTo[m - 1][n - 1];
    }
}