package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LRUCacheWithTimeConstraint")
class LRUCacheWthTTLTest {

    private LRUCacheWithTimeConstraint cache;


    @Test
    @DisplayName("LRUCacheWithTimeConstraint")
    void commitAfterRollbackOfSameTransactionThrows() {
        LRUCacheWithTimeConstraint cache = new LRUCacheWithTimeConstraint(2,  5);
        cache.put("A", "Apple", 1);
        cache.put("B", "Banana", 2);
        assertThat(cache.get("A", 3)).isEqualTo("Apple");
        cache.put("C", "Cherry", 4);
        assertThat(cache.get("B", 4)).isEqualTo("NOT_FOUND");
    }
    @Test
    @DisplayName("LRUCacheWithTimeConstraint1")
    void testLRUCacheWithTimeConstraintTest1() {
        LRUCacheWithTimeConstraint cache = new LRUCacheWithTimeConstraint( 2,  5);
        cache.put("A", "Apple", 1);
        cache.put("B", "Banana", 2);
        cache.put("A", "Apricot", 4);
        assertThat(cache.get("A", 8)).isEqualTo("Apricot");
        assertThat(cache.get("B", 8)).isEqualTo("NOT_FOUND");
        assertThat(cache.get("A", 89)).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("LRUCacheWithTimeConstraint2")
    void testLRUCacheWithTimeConstraintTest2() {
        LRUCacheWithTimeConstraint cache = new LRUCacheWithTimeConstraint( 1, 10);
        cache.put("A", "Apple", 1);
        cache.put("B", "Banana", 2);
        assertThat(cache.get("A", 3)).isEqualTo("NOT_FOUND");
        assertThat(cache.get("B", 3)).isEqualTo("Banana");
    }

    @Nested
    @DisplayName("Missing / empty cache")
    class MissingKeys {

        @Test
        @DisplayName("get on empty cache returns NOT_FOUND")
        void getOnEmptyCacheReturnsNotFound() {
            cache = new LRUCacheWithTimeConstraint(2, 5);
            assertThat(cache.get("A", 1)).isEqualTo("NOT_FOUND");
        }

        @Test
        @DisplayName("get on never-inserted key returns NOT_FOUND")
        void getOnUnknownKeyReturnsNotFound() {
            cache = new LRUCacheWithTimeConstraint(2, 5);
            cache.put("A", "Apple", 1);
            assertThat(cache.get("Z", 1)).isEqualTo("NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("Constructor validation")
    class ConstructorValidation {

        @Test
        @DisplayName("zero capacity is rejected")
        void zeroCapacityThrows() {
            assertThatThrownBy(() -> new LRUCacheWithTimeConstraint(0, 5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("negative capacity is rejected")
        void negativeCapacityThrows() {
            assertThatThrownBy(() -> new LRUCacheWithTimeConstraint(-1, 5))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("TTL expiry boundaries")
    class TtlBoundaries {

        @Test
        @DisplayName("entry is still valid one tick before expiry")
        void validJustBeforeExpiry() {
            cache = new LRUCacheWithTimeConstraint(2, 5);
            cache.put("A", "Apple", 1);
            assertThat(cache.get("A", 5)).isEqualTo("Apple");
        }

        @Test
        @DisplayName("entry expires exactly at expiresAt (inclusive boundary)")
        void expiresExactlyAtBoundary() {
            cache = new LRUCacheWithTimeConstraint(2, 5);
            cache.put("A", "Apple", 1);
            assertThat(cache.get("A", 6)).isEqualTo("NOT_FOUND");
        }

        @Test
        @DisplayName("ttl of zero expires immediately")
        void zeroTtlExpiresImmediately() {
            cache = new LRUCacheWithTimeConstraint(2, 0);
            cache.put("A", "Apple", 1);
            assertThat(cache.get("A", 1)).isEqualTo("NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("Update semantics")
    class UpdateSemantics {

        @Test
        @DisplayName("re-putting an existing key refreshes its TTL and does not grow the cache")
        void updateRefreshesTtlAndSize() {
            cache = new LRUCacheWithTimeConstraint(1, 5);
            cache.put("A", "Apple", 1);
            cache.put("A", "Apricot", 4);
            assertThat(cache.get("A", 8)).isEqualTo("Apricot");
            assertThat(cache.get("A", 9)).isEqualTo("NOT_FOUND");
        }

        @Test
        @DisplayName("re-putting an existing key marks it most recently used")
        void updateRefreshesLruPosition() {
            cache = new LRUCacheWithTimeConstraint(2, 100);
            cache.put("A", "Apple", 1);
            cache.put("B", "Banana", 2);
            cache.put("A", "Apricot", 3);
            cache.put("C", "Cherry", 4);
            assertThat(cache.get("B", 5)).isEqualTo("NOT_FOUND");
            assertThat(cache.get("A", 5)).isEqualTo("Apricot");
            assertThat(cache.get("C", 5)).isEqualTo("Cherry");
        }
    }

    @Nested
    @DisplayName("LRU eviction ordering")
    class LruEviction {

        @Test
        @DisplayName("get() on a key protects it from eviction (marks it MRU)")
        void getPromotesKeyToMostRecentlyUsed() {
            cache = new LRUCacheWithTimeConstraint(2, 100);
            cache.put("A", "Apple", 1);
            cache.put("B", "Banana", 2);
            cache.get("A", 3);
            cache.put("C", "Cherry", 4);
            assertThat(cache.get("B", 5)).isEqualTo("NOT_FOUND");
            assertThat(cache.get("A", 5)).isEqualTo("Apple");
            assertThat(cache.get("C", 5)).isEqualTo("Cherry");
        }

        @Test
        @DisplayName("least recently used entry is evicted first among multiple inserts")
        void evictsOldestOnRepeatedOverflow() {
            cache = new LRUCacheWithTimeConstraint(2, 100);
            cache.put("A", "Apple", 1);
            cache.put("B", "Banana", 2);
            cache.put("C", "Cherry", 3);
            cache.put("D", "Date", 4);
            assertThat(cache.get("A", 5)).isEqualTo("NOT_FOUND");
            assertThat(cache.get("B", 5)).isEqualTo("NOT_FOUND");
            assertThat(cache.get("C", 5)).isEqualTo("Cherry");
            assertThat(cache.get("D", 5)).isEqualTo("Date");
        }

        @Test
        @DisplayName("capacity of one always keeps only the most recent key")
        void capacityOneKeepsOnlyLatestKey() {
            cache = new LRUCacheWithTimeConstraint(1, 100);
            cache.put("A", "Apple", 1);
            cache.put("B", "Banana", 2);
            cache.put("C", "Cherry", 3);
            assertThat(cache.get("A", 4)).isEqualTo("NOT_FOUND");
            assertThat(cache.get("B", 4)).isEqualTo("NOT_FOUND");
            assertThat(cache.get("C", 4)).isEqualTo("Cherry");
        }
    }

    @Nested
    @DisplayName("Expired-entry cleanup interaction with capacity")
    class ExpiredCleanup {

        @Test
        @DisplayName("expired entries are swept on put and free up capacity for new keys")
        void expiredEntryFreesCapacityOnPut() {
            cache = new LRUCacheWithTimeConstraint(1, 2);
            cache.put("A", "Apple", 1);
            cache.put("B", "Banana", 10);
            assertThat(cache.get("A", 10)).isEqualTo("NOT_FOUND");
            assertThat(cache.get("B", 10)).isEqualTo("Banana");
        }
    }
}