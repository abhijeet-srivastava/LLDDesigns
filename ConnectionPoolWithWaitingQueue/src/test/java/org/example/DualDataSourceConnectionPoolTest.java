package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DualDataSourceConnectionPool")
class DualDataSourceConnectionPoolTest {

    private static final long THRESHOLD_MILLIS = 1000L;

    private static MutableClock mutableClock() {
        return new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Nested
    @DisplayName("acquireConnection")
    class AcquireConnection {

        @Test
        @DisplayName("Succeeds and returns a ConnectionPair when both datasources have room")
        void succeedsWhenBothDatasourcesHaveRoom() {
            DualDataSourceConnectionPool pool = new DualDataSourceConnectionPool(2, 2, THRESHOLD_MILLIS);

            DualDataSourceConnectionPool.ConnectionPair pair = pool.acquireConnection("req1");

            assertThat(pair).isEqualTo(new DualDataSourceConnectionPool.ConnectionPair(0, 0));
        }

        @Test
        @DisplayName("Lazily creates connections only up to capacity")
        void lazilyCreatesOnlyUpToCapacity() {
            DualDataSourceConnectionPool pool = new DualDataSourceConnectionPool(1, 1, THRESHOLD_MILLIS);
            DualDataSourceConnectionPool.ConnectionPair pair = pool.acquireConnection("req1");
            assertThat(pair.dataSource1ConnectionId()).isEqualTo(0);
            assertThat(pair.dataSource2ConnectionId()).isEqualTo(0);
            assertThat(pool.acquireConnection("req2")).isNull();
        }

        @Test
        @DisplayName("Stages the datasource1 connection for reuse when datasource2 is exhausted")
        void stagesDatasource1ConnectionWhenDatasource2Exhausted() {
            DualDataSourceConnectionPool pool = new DualDataSourceConnectionPool(2, 1, THRESHOLD_MILLIS);
            pool.acquireConnection("req1"); // ds1=0, ds2=0 -> succeeds, ds2 now exhausted

            DualDataSourceConnectionPool.ConnectionPair failed = pool.acquireConnection("req2");
            assertThat(failed).isNull(); // ds1=1 created but staged; ds2 still exhausted

            // A third acquire can only succeed by reusing the staged ds1 connection (id 1) once
            // ds2 frees up via releasing req1 -- ds1 capacity (2) is otherwise fully consumed.
            pool.releaseConnection("req1");
            DualDataSourceConnectionPool.ConnectionPair reused = pool.acquireConnection("req3");

            assertThat(reused).isNotNull();
            assertThat(reused.dataSource1ConnectionId()).isEqualTo(1);
        }

        @Test
        @DisplayName("Evicts a staged connection once the staleness threshold elapses, freeing capacity for a fresh one")
        void evictsStagedConnectionAfterThreshold() {
            MutableClock clock = mutableClock();
            DualDataSourceConnectionPool pool = new DualDataSourceConnectionPool(2, 1, THRESHOLD_MILLIS, clock);
            pool.acquireConnection("req1"); // ds1=0, ds2=0 -> succeeds, ds2 exhausted (capacity2=1)
            pool.acquireConnection("req2"); // ds1=1 created and staged (ds2 still exhausted)

            clock.advanceMillis(THRESHOLD_MILLIS + 1); // staged ds1=1 is now stale

            pool.releaseConnection("req1"); // frees ds1=0 and ds2=0 for reuse
            DualDataSourceConnectionPool.ConnectionPair req3Pair = pool.acquireConnection("req3");
            assertThat(req3Pair).isNotNull(); // evicts stale ds1=1, then reuses freed ds1=0/ds2=0

            // ds1 capacity (2) is fully occupied again (req3 active + evicted id never resurfaces),
            // but the eviction reclaimed a slot: this next attempt must lazily create a genuinely
            // new ds1 connection rather than exhausting immediately.
            pool.acquireConnection("req4"); // ds1=2 freshly created and staged; ds2 still exhausted

            pool.releaseConnection("req3"); // frees ds2=0 again
            DualDataSourceConnectionPool.ConnectionPair req5Pair = pool.acquireConnection("req5");

            assertThat(req5Pair).isNotNull();
            // proves req4's staged connection was a fresh id (2), never the evicted stale id (1)
            assertThat(req5Pair.dataSource1ConnectionId()).isEqualTo(2);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Abc", "a b", "a-b", "UPPER"})
        @DisplayName("Throws IllegalArgumentException for requestId with invalid characters")
        void throwsIllegalArgumentException_whenRequestIdHasInvalidChars(String requestId) {
            DualDataSourceConnectionPool pool = new DualDataSourceConnectionPool(1, 1, THRESHOLD_MILLIS);

            assertThatThrownBy(() -> pool.acquireConnection(requestId))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Throws IllegalArgumentException for null or empty requestId")
        void throwsIllegalArgumentException_whenRequestIdNullOrEmpty() {
            DualDataSourceConnectionPool pool = new DualDataSourceConnectionPool(1, 1, THRESHOLD_MILLIS);

            assertThatThrownBy(() -> pool.acquireConnection(null)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> pool.acquireConnection("")).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Throws IllegalStateException when requestId already holds a connection")
        void throwsIllegalStateException_whenDuplicateActiveRequestId() {
            DualDataSourceConnectionPool pool = new DualDataSourceConnectionPool(2, 2, THRESHOLD_MILLIS);
            pool.acquireConnection("req1");

            assertThatThrownBy(() -> pool.acquireConnection("req1"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("releaseConnection")
    class ReleaseConnection {

        @Test
        @DisplayName("Returns false for an unknown requestId")
        void returnsFalse_whenRequestIdUnknown() {
            DualDataSourceConnectionPool pool = new DualDataSourceConnectionPool(1, 1, THRESHOLD_MILLIS);

            assertThat(pool.releaseConnection("unknown")).isFalse();
        }

        @Test
        @DisplayName("Returns true and frees both connections for immediate reuse")
        void returnsTrueAndFreesBothConnections() {
            DualDataSourceConnectionPool pool = new DualDataSourceConnectionPool(1, 1, THRESHOLD_MILLIS);
            pool.acquireConnection("req1");

            assertThat(pool.releaseConnection("req1")).isTrue();
            assertThat(pool.acquireConnection("req2"))
                    .isEqualTo(new DualDataSourceConnectionPool.ConnectionPair(0, 0));
        }

        @Test
        @DisplayName("A second release of the same requestId returns false")
        void secondReleaseOfSameRequestIdReturnsFalse() {
            DualDataSourceConnectionPool pool = new DualDataSourceConnectionPool(1, 1, THRESHOLD_MILLIS);
            pool.acquireConnection("req1");
            pool.releaseConnection("req1");

            assertThat(pool.releaseConnection("req1")).isFalse();
        }
    }

    @Nested
    @DisplayName("getRequestsWithConnection")
    class GetRequestsWithConnection {

        @Test
        @DisplayName("Returns an empty list when nothing is active")
        void returnsEmptyList_whenNoActiveConnections() {
            DualDataSourceConnectionPool pool = new DualDataSourceConnectionPool(2, 2, THRESHOLD_MILLIS);

            assertThat(pool.getRequestsWithConnection()).isEmpty();
        }

        @Test
        @DisplayName("Returns correct entries after a mixed acquire/release sequence")
        void returnsCorrectEntriesAfterMixedAcquireRelease() {
            DualDataSourceConnectionPool pool = new DualDataSourceConnectionPool(2, 2, THRESHOLD_MILLIS);
            pool.acquireConnection("req1");
            pool.acquireConnection("req2");
            pool.releaseConnection("req1");
            pool.acquireConnection("req3");

            assertThat(pool.getRequestsWithConnection())
                    .containsExactlyInAnyOrder("req2-1-1", "req3-0-0");
        }
    }

    @Nested
    @DisplayName("concurrency")
    class Concurrency {

        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("Never double-assigns a connection id under concurrent acquire, and caps successes at min(capacity1, capacity2)")
        void neverDoubleAssignsUnderConcurrentAcquire() throws InterruptedException {
            int capacity = 5;
            int threadCount = 15;
            DualDataSourceConnectionPool pool = new DualDataSourceConnectionPool(capacity, capacity, THRESHOLD_MILLIS);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            List<DualDataSourceConnectionPool.ConnectionPair> results = new CopyOnWriteArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                String requestId = "req" + i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        DualDataSourceConnectionPool.ConnectionPair pair = pool.acquireConnection(requestId);
                        if (pair != null) {
                            results.add(pair);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            doneLatch.await();
            executor.shutdown();

            Set<Integer> ds1Ids = results.stream().map(DualDataSourceConnectionPool.ConnectionPair::dataSource1ConnectionId).collect(Collectors.toSet());
            Set<Integer> ds2Ids = results.stream().map(DualDataSourceConnectionPool.ConnectionPair::dataSource2ConnectionId).collect(Collectors.toSet());

            assertThat(results).hasSize(capacity);
            assertThat(ds1Ids).hasSameSizeAs(results);
            assertThat(ds2Ids).hasSameSizeAs(results);
        }
    }

    /** A Clock whose instant can be advanced on demand, for deterministic staleness tests. */
    private static final class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advanceMillis(long millis) {
            instant = instant.plusMillis(millis);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}