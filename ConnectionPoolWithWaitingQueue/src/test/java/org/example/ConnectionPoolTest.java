package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ConnectionPool")
class ConnectionPoolTest {

    @Nested
    @DisplayName("acquireConnection")
    class AcquireConnection {

        @Test
        @DisplayName("Returns the minimum-index free connection")
        void returnsMinimumIndexFreeConnection() {
            ConnectionPool pool = new ConnectionPool(3);

            assertThat(pool.acquireConnection("req1")).isEqualTo(0);
        }

        @Test
        @DisplayName("Returns connections in ascending order as they are acquired")
        void returnsAscendingIndicesAcrossSuccessiveAcquires() {
            ConnectionPool pool = new ConnectionPool(3);

            assertThat(pool.acquireConnection("req1")).isEqualTo(0);
            assertThat(pool.acquireConnection("req2")).isEqualTo(1);
            assertThat(pool.acquireConnection("req3")).isEqualTo(2);
        }

        @Test
        @DisplayName("Returns -1 and enqueues the request when the pool is exhausted")
        void returnsMinusOneAndEnqueuesWhenPoolExhausted() {
            ConnectionPool pool = new ConnectionPool(1);
            pool.acquireConnection("req1");

            assertThat(pool.acquireConnection("req2")).isEqualTo(-1);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Abc", "a b", "a-b", "UPPER", "with_underscore"})
        @DisplayName("Throws IllegalArgumentException for requestId with invalid characters")
        void throwsIllegalArgumentException_whenRequestIdHasInvalidChars(String requestId) {
            ConnectionPool pool = new ConnectionPool(1);

            assertThatThrownBy(() -> pool.acquireConnection(requestId))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Throws IllegalArgumentException for null requestId")
        void throwsIllegalArgumentException_whenRequestIdNull() {
            ConnectionPool pool = new ConnectionPool(1);

            assertThatThrownBy(() -> pool.acquireConnection(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Throws IllegalArgumentException for empty requestId")
        void throwsIllegalArgumentException_whenRequestIdEmpty() {
            ConnectionPool pool = new ConnectionPool(1);

            assertThatThrownBy(() -> pool.acquireConnection(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Throws IllegalStateException when requestId already holds a connection")
        void throwsIllegalStateException_whenDuplicateActiveRequestId() {
            ConnectionPool pool = new ConnectionPool(2);
            pool.acquireConnection("req1");

            assertThatThrownBy(() -> pool.acquireConnection("req1"))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Throws IllegalStateException when requestId is already waiting")
        void throwsIllegalStateException_whenDuplicateWaitingRequestId() {
            ConnectionPool pool = new ConnectionPool(1);
            pool.acquireConnection("req1");
            pool.acquireConnection("req2");

            assertThatThrownBy(() -> pool.acquireConnection("req2"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("releaseConnection")
    class ReleaseConnection {

        @Test
        @DisplayName("Returns false for an unknown requestId")
        void returnsFalse_whenRequestIdUnknown() {
            ConnectionPool pool = new ConnectionPool(1);

            assertThat(pool.releaseConnection("unknown")).isFalse();
        }

        @Test
        @DisplayName("Returns true and frees the connection when there are no waiters")
        void returnsTrueAndFreesConnection_whenNoWaiters() {
            ConnectionPool pool = new ConnectionPool(1);
            pool.acquireConnection("req1");

            assertThat(pool.releaseConnection("req1")).isTrue();
            assertThat(pool.acquireConnection("req2")).isEqualTo(0);
        }

        @Test
        @DisplayName("Reassigns the released connection to the oldest waiter")
        void reassignsReleasedConnection_toOldestWaiter() {
            ConnectionPool pool = new ConnectionPool(1);
            int connectionId = pool.acquireConnection("req1");
            pool.acquireConnection("req2");

            pool.releaseConnection("req1");

            assertThat(pool.getRequestsWithConnection()).containsExactly("req2-" + connectionId);
        }

        @Test
        @DisplayName("Preserves FIFO order across multiple waiters and releases")
        void preservesFifoOrderAcrossMultipleWaitersAndReleases() {
            ConnectionPool pool = new ConnectionPool(1);
            pool.acquireConnection("req1");
            pool.acquireConnection("req2");
            pool.acquireConnection("req3");

            pool.releaseConnection("req1");
            assertThat(pool.getRequestsWithConnection()).containsExactly("req2-0");

            pool.releaseConnection("req2");
            assertThat(pool.getRequestsWithConnection()).containsExactly("req3-0");
        }

        @Test
        @DisplayName("A requestId can reacquire after releasing")
        void requestIdCanReacquireAfterRelease() {
            ConnectionPool pool = new ConnectionPool(1);
            pool.acquireConnection("req1");
            pool.releaseConnection("req1");

            assertThat(pool.acquireConnection("req1")).isEqualTo(0);
        }

        @Test
        @DisplayName("A second release of the same requestId returns false")
        void secondReleaseOfSameRequestIdReturnsFalse() {
            ConnectionPool pool = new ConnectionPool(1);
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
            ConnectionPool pool = new ConnectionPool(2);

            assertThat(pool.getRequestsWithConnection()).isEmpty();
        }

        @Test
        @DisplayName("Excludes waiting requests, only lists active holders")
        void excludesWaitingRequests() {
            ConnectionPool pool = new ConnectionPool(1);
            pool.acquireConnection("req1");
            pool.acquireConnection("req2");

            assertThat(pool.getRequestsWithConnection()).containsExactly("req1-0");
        }

        @Test
        @DisplayName("Returns correct requestId-connectionId pairs after a mixed acquire/release sequence")
        void returnsCorrectPairsAfterMixedAcquireRelease() {
            ConnectionPool pool = new ConnectionPool(2);
            pool.acquireConnection("req1");
            pool.acquireConnection("req2");
            pool.releaseConnection("req1");
            pool.acquireConnection("req3");

            assertThat(pool.getRequestsWithConnection())
                    .containsExactlyInAnyOrder("req2-1", "req3-0");
        }
    }

    @Nested
    @DisplayName("concurrency")
    class Concurrency {

        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("Never double-assigns the same connection id under concurrent acquire")
        void neverDoubleAssignsSameConnectionId_underConcurrentAcquire() throws InterruptedException {
            int capacity = 5;
            int threadCount = 15;
            ConnectionPool pool = new ConnectionPool(capacity);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            List<Integer> results = new CopyOnWriteArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                String requestId = "req" + i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        results.add(pool.acquireConnection(requestId));
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

            List<Integer> assignedIds = results.stream().filter(id -> id != -1).collect(Collectors.toList());
            Set<Integer> distinctAssignedIds = assignedIds.stream().collect(Collectors.toSet());

            assertThat(assignedIds).hasSize(capacity);
            assertThat(distinctAssignedIds).hasSameSizeAs(assignedIds);
        }

        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("Maintains consistent state after concurrent acquire and release loops")
        void maintainsConsistentState_afterConcurrentAcquireAndRelease() throws InterruptedException {
            int capacity = 5;
            int threadCount = 15;
            ConnectionPool pool = new ConnectionPool(capacity);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger counter = new AtomicInteger();

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < 20; j++) {
                            String requestId = "worker" + counter.incrementAndGet();
                            int connectionId = pool.acquireConnection(requestId);
                            if (connectionId != -1) {
                                pool.releaseConnection(requestId);
                            }
                        }
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            doneLatch.await();
            executor.shutdown();

            assertThat(pool.getRequestsWithConnection().size()).isLessThanOrEqualTo(capacity);
        }
    }
}