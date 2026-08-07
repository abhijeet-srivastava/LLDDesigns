package org.example;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Simulates acquiring a connection pair from two independent datasources whose
 * connections are created lazily, up to each datasource's capacity. acquireConnection
 * never blocks: if datasource2 has no connection available, the datasource1 connection
 * just acquired for the attempt is not discarded but returned to its free pool, where it
 * (like any free connection from either datasource) expires if unclaimed after a
 * configured time-to-live.
 */
public class DualDataSourceConnectionPool {

    public record ConnectionPair(int dataSource1ConnectionId, int dataSource2ConnectionId) {}

    /** A free connection awaiting reuse; expires and is discarded after expiresAt. */
    private record FreeConnection(int id, Instant expiresAt) {}

    private static final Pattern VALID_REQUEST_ID = Pattern.compile("^[a-z0-9]+$");

    private final int capacity1;
    private final int capacity2;
    private final long ttlMillis;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();

    private int nextDs1Id = 0;
    private int ds1ExistingCount = 0;
    private int nextDs2Id = 0;
    private int ds2ExistingCount = 0;

    private final Deque<FreeConnection> freeDs1 = new ArrayDeque<>();
    private final Deque<FreeConnection> freeDs2 = new ArrayDeque<>();
    private final Map<String, ConnectionPair> requestToConnections = new HashMap<>();

    public DualDataSourceConnectionPool(int capacity1, int capacity2, long ttlMillis) {
        this(capacity1, capacity2, ttlMillis, Clock.systemUTC());
    }

    DualDataSourceConnectionPool(int capacity1, int capacity2, long ttlMillis, Clock clock) {
        if (capacity1 <= 0 || capacity2 <= 0) {
            throw new IllegalArgumentException("capacities must be positive");
        }
        this.capacity1 = capacity1;
        this.capacity2 = capacity2;
        this.ttlMillis = ttlMillis;
        this.clock = clock;
    }

    /**
     * Acquires a connection to both datasources for the given request. Never blocks.
     *
     * @param requestId non-null, non-empty, lowercase letters and digits only
     * @return the assigned {@link ConnectionPair}, or {@code null} if a connection to
     *         either datasource isn't currently available
     * @throws IllegalArgumentException if requestId is null, empty, or contains invalid characters
     * @throws IllegalStateException if requestId already holds a connection pair
     */
    public ConnectionPair acquireConnection(String requestId) {
        if (requestId == null || !VALID_REQUEST_ID.matcher(requestId).matches()) {
            throw new IllegalArgumentException("requestId must be non-empty lowercase letters/digits");
        }
        lock.lock();
        try {
            if (requestToConnections.containsKey(requestId)) {
                throw new IllegalStateException("requestId already has a connection: " + requestId);
            }

            evictExpired(freeDs1, () -> ds1ExistingCount--);
            evictExpired(freeDs2, () -> ds2ExistingCount--);

            Integer ds1Id = acquireDs1Connection();
            if (ds1Id == null) {
                return null;
            }

            Integer ds2Id = acquireDs2Connection();
            if (ds2Id == null) {
                freeDs1.addLast(new FreeConnection(ds1Id, clock.instant().plusMillis(ttlMillis)));
                return null;
            }

            ConnectionPair pair = new ConnectionPair(ds1Id, ds2Id);
            requestToConnections.put(requestId, pair);
            return pair;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Releases the connection pair held by the given request, returning both connections
     * to their free pools (subject to the same time-to-live as any other free connection).
     *
     * @param requestId the request releasing its connections
     * @return true if requestId held a connection pair and it was released, false otherwise
     */
    public boolean releaseConnection(String requestId) {
        lock.lock();
        try {
            ConnectionPair pair = requestToConnections.remove(requestId);
            if (pair == null) {
                return false;
            }
            Instant expiresAt = clock.instant().plusMillis(ttlMillis);
            freeDs1.addLast(new FreeConnection(pair.dataSource1ConnectionId(), expiresAt));
            freeDs2.addLast(new FreeConnection(pair.dataSource2ConnectionId(), expiresAt));
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return "requestId-dataSource1ConnectionId-dataSource2ConnectionId" entries for every
     *         request currently holding a connection pair
     */
    public List<String> getRequestsWithConnection() {
        lock.lock();
        try {
            return requestToConnections.entrySet().stream()
                    .map(e -> e.getKey() + "-" + e.getValue().dataSource1ConnectionId()
                            + "-" + e.getValue().dataSource2ConnectionId())
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    private void evictExpired(Deque<FreeConnection> freeConnections, Runnable onEvict) {
        Instant now = clock.instant();
        freeConnections.removeIf(entry -> {
            boolean expired = entry.expiresAt().isBefore(now);
            if (expired) {
                onEvict.run();
            }
            return expired;
        });
    }

    private Integer acquireDs1Connection() {
        if (!freeDs1.isEmpty()) {
            return freeDs1.pollFirst().id();
        }
        if (ds1ExistingCount < capacity1) {
            ds1ExistingCount++;
            return nextDs1Id++;
        }
        return null;
    }

    private Integer acquireDs2Connection() {
        if (!freeDs2.isEmpty()) {
            return freeDs2.pollFirst().id();
        }
        if (ds2ExistingCount < capacity2) {
            ds2ExistingCount++;
            return nextDs2Id++;
        }
        return null;
    }
}