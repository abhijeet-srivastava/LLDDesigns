package org.example;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A fixed-size, in-memory pool of {@code capacity} reusable connections indexed
 * {@code 0..capacity-1}. Requests that arrive when no connection is free are queued
 * FIFO and are handed a connection as soon as one is released. Safe for concurrent use.
 */
public class ConnectionPool {

    private static final Pattern VALID_REQUEST_ID = Pattern.compile("^[a-z0-9]+$");

    private final TreeSet<Integer> freeConnections = new TreeSet<>();
    private final Map<String, Integer> requestToConnection = new HashMap<>();
    private final Deque<String> waitingQueue = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();

    public ConnectionPool(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        for (int i = 0; i < capacity; i++) {
            freeConnections.add(i);
        }
    }

    /**
     * Acquires a connection for the given request.
     *
     * @param requestId non-null, non-empty, lowercase letters and digits only
     * @return the minimum-index free connection assigned to this request, or -1 if none
     *         were free and the request was enqueued to wait
     * @throws IllegalArgumentException if requestId is null, empty, or contains invalid characters
     * @throws IllegalStateException if requestId already holds a connection or is already waiting
     */
    public int acquireConnection(String requestId) {
        if (requestId == null || !VALID_REQUEST_ID.matcher(requestId).matches()) {
            throw new IllegalArgumentException("requestId must be non-empty lowercase letters/digits");
        }
        lock.lock();
        try {
            if (requestToConnection.containsKey(requestId) || waitingQueue.contains(requestId)) {
                throw new IllegalStateException("requestId already has or is waiting for a connection: " + requestId);
            }
            if (!freeConnections.isEmpty()) {
                int connectionId = freeConnections.pollFirst();
                requestToConnection.put(requestId, connectionId);
                return connectionId;
            }
            waitingQueue.addLast(requestId);
            return -1;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Releases the connection held by the given request. If requests are waiting, the
     * released connection is immediately reassigned to the oldest waiter.
     *
     * @param requestId the request releasing its connection
     * @return true if requestId held a connection and it was released, false otherwise
     */
    public boolean releaseConnection(String requestId) {
        lock.lock();
        try {
            Integer connectionId = requestToConnection.remove(requestId);
            if (connectionId == null) {
                return false;
            }
            if (!waitingQueue.isEmpty()) {
                String nextRequestId = waitingQueue.pollFirst();
                requestToConnection.put(nextRequestId, connectionId);
            } else {
                freeConnections.add(connectionId);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return "requestId-connectionId" entries for every request currently holding a connection
     */
    public List<String> getRequestsWithConnection() {
        lock.lock();
        try {
            return requestToConnection.entrySet().stream()
                    .map(e -> e.getKey() + "-" + e.getValue())
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }
}
