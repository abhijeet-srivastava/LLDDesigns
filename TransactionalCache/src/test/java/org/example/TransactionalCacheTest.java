package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionalCacheTest {

    private Cache cache;

    @BeforeEach
    void setUp() {
        cache = new TransactionalCache();
    }

    @Test
    void getReturnsNullForMissingKey() {
        assertNull(cache.get("missing"));
    }

    @Test
    void putAndGetWithoutTransactionAppliesImmediately() {
        cache.put("key1", "v1");
        assertEquals("v1", cache.get("key1"));

        cache.delete("key1");
        assertNull(cache.get("key1"));
    }

    @Test
    void keySetInOneTransactionIsVisibleAcrossSeveralNestedTransactions() {
        cache.transaction();
        cache.put("key1", "v1");

        // Open several more nested transactions that don't touch key1.
        cache.transaction();
        cache.transaction();
        cache.transaction();

        assertEquals("v1", cache.get("key1"), "key1 should remain visible through unrelated nested transactions");
    }

    @Test
    void keySetInOuterTransactionSurvivesRollbackOfInnerTransactions() {
        cache.transaction();
        cache.put("key1", "v1");

        cache.transaction();
        cache.put("key2", "v2");
        cache.rollback();

        assertEquals("v1", cache.get("key1"));
        assertNull(cache.get("key2"));
    }

    @Test
    void innerTransactionOverrideIsHiddenAfterRollback() {
        cache.put("key1", "base");

        cache.transaction();
        cache.put("key1", "outer");

        cache.transaction();
        cache.put("key1", "inner");
        assertEquals("inner", cache.get("key1"));

        cache.rollback();
        assertEquals("outer", cache.get("key1"), "rollback of inner txn should expose outer txn's value");

        cache.rollback();
        assertEquals("base", cache.get("key1"), "rollback of outer txn should expose base value");
    }

    @Test
    void deleteInNestedTransactionHidesValueUntilRollback() {
        cache.put("key1", "base");

        cache.transaction();
        cache.transaction();
        cache.delete("key1");
        assertNull(cache.get("key1"));

        cache.rollback();
        assertEquals("base", cache.get("key1"));
    }

    @Test
    void commitMergesChangesIntoParentTransactionNotBase() {
        cache.transaction();
        cache.put("key1", "outer");

        cache.transaction();
        cache.put("key1", "inner");
        cache.commit();

        // Change merged into outer transaction, base store untouched.
        assertEquals("inner", cache.get("key1"));

        cache.rollback();
        assertNull(cache.get("key1"), "rolling back outer txn should undo everything, base was never touched");
    }

    @Test
    void commitOfOutermostTransactionPersistsToBaseStore() {
        cache.transaction();
        cache.put("key1", "v1");
        cache.commit();

        assertEquals("v1", cache.get("key1"));

        // No transaction active anymore; value must still be readable directly from base.
        assertEquals("v1", cache.get("key1"));
    }

    @Test
    void commitOfDeleteRemovesKeyFromBaseStore() {
        cache.put("key1", "v1");

        cache.transaction();
        cache.delete("key1");
        cache.commit();

        assertNull(cache.get("key1"));
    }

    @Test
    void transactionIdsAreSequential() {
        int id1 = cache.transaction();
        int id2 = cache.transaction();
        int id3 = cache.transaction();

        assertTrue(id2 > id1);
        assertTrue(id3 > id2);
    }

    @Test
    void commitWithoutActiveTransactionThrows() {
        assertThrows(IllegalStateException.class, () -> cache.commit());
    }

    @Test
    void rollbackWithoutActiveTransactionThrows() {
        assertThrows(IllegalStateException.class, () -> cache.rollback());
    }

    @Test
    void commitAfterRollbackOfSameTransactionThrows() {
        cache.transaction();
        cache.put("key1", "v1");
        cache.rollback();

        assertThrows(IllegalStateException.class, () -> cache.commit());
    }
}