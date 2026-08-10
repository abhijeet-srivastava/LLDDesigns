package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomHashMap")
class CustomHashMapTest {

    private CustomHashMap map;

    @BeforeEach
    void setUp() {
        map = new CustomHashMap(0.0, 100.0);
    }

    @Nested
    @DisplayName("put")
    class Put {

        @Test
        @DisplayName("inserts a new key and increases size")
        void insertsNewKey() {
            map.put("a", "1");

            assertThat(map.size()).isEqualTo(1);
            assertThat(map.get("a")).isEqualTo("1");
        }

        @Test
        @DisplayName("updates the value of an existing key without increasing size")
        void updatesExistingKeyValue() {
            map.put("a", "1");
            map.put("a", "2");

            assertThat(map.size()).isEqualTo(1);
            assertThat(map.get("a")).isEqualTo("2");
        }

        @Test
        @DisplayName("chains colliding keys within the same bucket")
        void handlesCollisionsViaChaining() {
            // "a" (97) and "c" (99) both hash to bucket 1 of a 2-bucket map
            map.put("a", "1");
            map.put("c", "2");

            assertThat(map.get("a")).isEqualTo("1");
            assertThat(map.get("c")).isEqualTo("2");
            assertThat(map.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("returns the stored value for an existing key")
        void returnsStoredValue() {
            map.put("key", "value");

            assertThat(map.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("returns empty string for a missing key")
        void returnsEmptyStringForMissingKey() {
            assertThat(map.get("missing")).isEqualTo("");
        }

        @Test
        @DisplayName("returns the updated value after put overwrites it")
        void returnsUpdatedValueAfterOverwrite() {
            map.put("key", "old");
            map.put("key", "new");

            assertThat(map.get("key")).isEqualTo("new");
        }
    }

    @Nested
    @DisplayName("remove")
    class Remove {

        @Test
        @DisplayName("removes an existing key and returns its value")
        void removesExistingKey() {
            map.put("key", "value");

            assertThat(map.remove("key")).isEqualTo("value");
            assertThat(map.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("returns empty string when removing a missing key, size unchanged")
        void removingMissingKeyIsNoOp() {
            map.put("a", "1");

            assertThat(map.remove("missing")).isEqualTo("");
            assertThat(map.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("get returns empty string after removal")
        void getAfterRemoveReturnsEmptyString() {
            map.put("key", "value");
            map.remove("key");

            assertThat(map.get("key")).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("getBucketKeys")
    class GetBucketKeys {

        @Test
        @DisplayName("returns keys of a valid bucket sorted lexicographically")
        void returnsSortedKeysForValidBucket() {
            // "a" (97) and "c" (99) both hash to bucket 1 of a 2-bucket map
            map.put("c", "3");
            map.put("a", "1");

            assertThat(map.getBucketKeys(0)).containsExactly("a", "c");
        }

        @Test
        @DisplayName("returns empty list for a negative index")
        void returnsEmptyListForNegativeIndex() {
            assertThat(map.getBucketKeys(-1)).isEmpty();
        }

        @Test
        @DisplayName("returns empty list for an out-of-range index")
        void returnsEmptyListForOutOfRangeIndex() {
            assertThat(map.getBucketKeys(map.bucketsCount())).isEmpty();
        }

        @Test
        @DisplayName("returns empty list for a valid but unused bucket")
        void returnsEmptyListForUnusedBucket() {
            assertThat(map.getBucketKeys(0)).isEmpty();
        }
    }

    @Nested
    @DisplayName("size and bucketsCount")
    class Size {

        @Test
        @DisplayName("size starts at 0 and bucketsCount starts at 2")
        void startsEmpty() {
            assertThat(map.size()).isEqualTo(0);
            assertThat(map.bucketsCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("size tracks puts and removes accurately")
        void sizeTracksMutations() {
            map.put("a", "1");
            map.put("b", "2");
            map.put("a", "3");
            map.remove("b");

            assertThat(map.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("rehashing")
    class Rehashing {

        @Test
        @DisplayName("grows buckets when load factor exceeds maxLoadFactor")
        void growsOnHighLoadFactor() {
            CustomHashMap small = new CustomHashMap(0.0, 0.5);

            small.put("a", "1"); // size=1, buckets=2, lf=0.5 -> not > 0.5, stays at 2
            small.put("b", "2"); // size=2, buckets=2, lf=1.0 -> > 0.5, doubles to 4, lf=0.5, stops

            assertThat(small.bucketsCount()).isEqualTo(4);
            assertThat(small.size()).isEqualTo(2);
            assertThat(small.get("a")).isEqualTo("1");
            assertThat(small.get("b")).isEqualTo("2");
        }

        @Test
        @DisplayName("shrinks buckets when load factor drops below minLoadFactor")
        void shrinksOnLowLoadFactor() {
            CustomHashMap small = new CustomHashMap(0.4, 1.0);

            small.put("a", "1"); // buckets=2, lf=0.5
            small.put("b", "2"); // buckets=2, lf=1.0 -> grows to 4, lf=0.5
            small.put("c", "3"); // buckets=4, lf=0.75

            small.remove("c"); // size=2, buckets=4, lf=0.5 -> not < 0.4, stays at 4
            small.remove("b"); // size=1, buckets=4, lf=0.25 -> < 0.4, shrinks to 2, lf=0.5

            assertThat(small.bucketsCount()).isEqualTo(2);
            assertThat(small.size()).isEqualTo(1);
            assertThat(small.get("a")).isEqualTo("1");
        }

        @Test
        @DisplayName("never shrinks below 2 buckets")
        void neverShrinksBelowFloor() {
            CustomHashMap small = new CustomHashMap(0.9, 1.0);

            small.put("a", "1");
            small.remove("a");

            assertThat(small.bucketsCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("data survives multiple grow/shrink cycles")
        void dataSurvivesResizeCycles() {
            CustomHashMap small = new CustomHashMap(0.2, 0.6);

            small.put("a", "1");
            small.put("b", "2");
            small.put("c", "3");
            small.put("d", "4");
            small.remove("b");
            small.remove("c");
            small.remove("d");

            assertThat(small.get("a")).isEqualTo("1");
            assertThat(small.size()).isEqualTo(1);
            assertThat(small.bucketsCount()).isGreaterThanOrEqualTo(2);
        }
    }
}