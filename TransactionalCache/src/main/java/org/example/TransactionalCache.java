package org.example;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class TransactionalCache implements Cache {

    private static final Object TOMBSTONE = new Object();

    private static class Transaction {
        final int id;
        final Map<String, Object> changes = new HashMap<>();

        Transaction(int id) {
            this.id = id;
        }
    }

    private final Map<String, Object> baseStore = new HashMap<>();
    private final Deque<Transaction> transactionStack = new ArrayDeque<>();
    private int nextTransactionId = 1;

    @Override
    public Object get(String key) {
        for (Transaction txn : transactionStack) {
            if (txn.changes.containsKey(key)) {
                Object value = txn.changes.get(key);
                return value == TOMBSTONE ? null : value;
            }
        }
        return baseStore.get(key);
    }

    @Override
    public void put(String key, Object value) {
        if (transactionStack.isEmpty()) {
            baseStore.put(key, value);
        } else {
            transactionStack.peek().changes.put(key, value);
        }
    }

    @Override
    public void delete(String key) {
        if (transactionStack.isEmpty()) {
            baseStore.remove(key);
        } else {
            transactionStack.peek().changes.put(key, TOMBSTONE);
        }
    }

    @Override
    public int transaction() {
        Transaction txn = new Transaction(nextTransactionId++);
        transactionStack.push(txn);
        return txn.id;
    }

    @Override
    public void commit() {
        if (transactionStack.isEmpty()) {
            throw new IllegalStateException("No active transaction to commit");
        }
        Transaction txn = transactionStack.pop();
        Map<String, Object> target = transactionStack.isEmpty() ? baseStore : transactionStack.peek().changes;
        for (Map.Entry<String, Object> entry : txn.changes.entrySet()) {
            if (target == baseStore && entry.getValue() == TOMBSTONE) {
                target.remove(entry.getKey());
            } else {
                target.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void rollback() {
        if (transactionStack.isEmpty()) {
            throw new IllegalStateException("No active transaction to roll back");
        }
        transactionStack.pop();
    }
}