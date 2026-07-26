package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TransactionalCache")
class TransactionalCacheTest {

    private Cache cache;

    @BeforeEach
    void setUp() {
        cache = new TransactionalCache();
    }

    @Nested
    @DisplayName("with no active transaction")
    class NoActiveTransaction {

        @Test
        @DisplayName("Should return null for missing key")
        void getReturnsNullForMissingKey() {
            assertThat(cache.get("missing")).isNull();
        }

        @Test
        @DisplayName("Should apply put and delete immediately")
        void putAndGetWithoutTransactionAppliesImmediately() {
            cache.put("key1", "v1");
            assertThat(cache.get("key1")).isEqualTo("v1");

            cache.delete("key1");
            assertThat(cache.get("key1")).isNull();
        }
    }

    @Nested
    @DisplayName("with nested transactions")
    class NestedTransactions {

        @Test
        @DisplayName("Should keep key set in one transaction visible across unrelated nested transactions")
        void keySetInOneTransactionIsVisibleAcrossSeveralNestedTransactions() {
            cache.transaction();
            cache.put("key1", "v1");

            // Open several more nested transactions that don't touch key1.
            cache.transaction();
            cache.transaction();
            cache.transaction();

            assertThat(cache.get("key1"))
                    .as("key1 should remain visible through unrelated nested transactions")
                    .isEqualTo("v1");
        }

        @Test
        @DisplayName("Should keep key set in outer transaction after rollback of inner transactions")
        void keySetInOuterTransactionSurvivesRollbackOfInnerTransactions() {
            cache.transaction();
            cache.put("key1", "v1");

            cache.transaction();
            cache.put("key2", "v2");
            cache.rollback();

            assertThat(cache.get("key1")).isEqualTo("v1");
            assertThat(cache.get("key2")).isNull();
        }

        @Test
        @DisplayName("Should hide inner transaction override after rollback")
        void innerTransactionOverrideIsHiddenAfterRollback() {
            cache.put("key1", "base");

            cache.transaction();
            cache.put("key1", "outer");

            cache.transaction();
            cache.put("key1", "inner");
            assertThat(cache.get("key1")).isEqualTo("inner");

            cache.rollback();
            assertThat(cache.get("key1"))
                    .as("rollback of inner txn should expose outer txn's value")
                    .isEqualTo("outer");

            cache.rollback();
            assertThat(cache.get("key1"))
                    .as("rollback of outer txn should expose base value")
                    .isEqualTo("base");
        }

        @Test
        @DisplayName("Should hide value deleted in nested transaction until rollback")
        void deleteInNestedTransactionHidesValueUntilRollback() {
            cache.put("key1", "base");

            cache.transaction();
            cache.transaction();
            cache.delete("key1");
            assertThat(cache.get("key1")).isNull();

            cache.rollback();
            assertThat(cache.get("key1")).isEqualTo("base");
        }
    }

    @Nested
    @DisplayName("on commit")
    class Commit {

        @Test
        @DisplayName("Should merge changes into parent transaction, not base")
        void commitMergesChangesIntoParentTransactionNotBase() {
            cache.transaction();
            cache.put("key1", "outer");

            cache.transaction();
            cache.put("key1", "inner");
            cache.commit();

            // Change merged into outer transaction, base store untouched.
            assertThat(cache.get("key1")).isEqualTo("inner");

            cache.rollback();
            assertThat(cache.get("key1"))
                    .as("rolling back outer txn should undo everything, base was never touched")
                    .isNull();
        }

        @Test
        @DisplayName("Should persist to base store when committing the outermost transaction")
        void commitOfOutermostTransactionPersistsToBaseStore() {
            cache.transaction();
            cache.put("key1", "v1");
            cache.commit();

            assertThat(cache.get("key1")).isEqualTo("v1");

            // No transaction active anymore; value must still be readable directly from base.
            assertThat(cache.get("key1")).isEqualTo("v1");
        }

        @Test
        @DisplayName("Should remove key from base store when committing a delete")
        void commitOfDeleteRemovesKeyFromBaseStore() {
            cache.put("key1", "v1");

            cache.transaction();
            cache.delete("key1");
            cache.commit();

            assertThat(cache.get("key1")).isNull();
        }
    }

    @Nested
    @DisplayName("error cases")
    class ErrorCases {

        @Test
        @DisplayName("Should assign sequential transaction ids")
        void transactionIdsAreSequential() {
            int id1 = cache.transaction();
            int id2 = cache.transaction();
            int id3 = cache.transaction();

            assertThat(id2).isGreaterThan(id1);
            assertThat(id3).isGreaterThan(id2);
        }

        @Test
        @DisplayName("Should throw when committing without an active transaction")
        void commitWithoutActiveTransactionThrows() {
            assertThatThrownBy(() -> cache.commit())
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Should throw when rolling back without an active transaction")
        void rollbackWithoutActiveTransactionThrows() {
            assertThatThrownBy(() -> cache.rollback())
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Should throw when committing after rollback of the same transaction")
        void commitAfterRollbackOfSameTransactionThrows() {
            cache.transaction();
            cache.put("key1", "v1");
            cache.rollback();

            assertThatThrownBy(() -> cache.commit())
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}