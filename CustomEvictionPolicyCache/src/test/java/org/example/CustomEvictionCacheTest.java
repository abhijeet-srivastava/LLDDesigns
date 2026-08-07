package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("CustomEvictionCacheTest")
class CustomEvictionCacheTest {

    @BeforeEach
    void setUp() {
        System.out.println("Class level cache set up");
    }

    @Nested
    @DisplayName("Test largest Evicition policy")
    class TestLargestEvictionPolicy {
        private Cache cache;
        @BeforeEach
        void setUp() {
            cache = new CustomEvictionCache(2, new RemoveLargestEvictionPolicy());
        }
        @Test
        @DisplayName("Should return empty for missing key")
        void testGetMissingKey() {
            String val = cache.get("abc");
            assertThat(val).isEmpty();
        }
        @Test
        @DisplayName("Should return value saved corresponding to key")
        void testGetExistingKey() {
            cache.put("key1", "val1");
            String val = cache.get("key1");
            assertThat(val).isEqualTo("val1");
        }

        @Test
        @DisplayName("Validate Eviction")
        void testEviction() {
            cache.put("a",   "v1");
            cache.put("bb",  "v2");
            String nxtKey = cache.nextEvictionKey();
            assertThat(nxtKey).isEqualTo("bb");
            cache.put("ccc", "v3");
            assertThat(cache.get("ccc")).isEmpty();
            assertThat(cache.get("bb")).isEqualTo("v2");
        }

        @Test
        @DisplayName("Validate Eviction If tie")
        void testRemoveSmallestIfTie() {
            cache = new CustomEvictionCache(3, new RemoveLargestEvictionPolicy());
            cache.put("bb", "1");          // cache: { bb }
            cache.put("aa", "2");         // cache: { bb }
            cache.put("cc", "3");         // cache: { bb }
            String nxtKey = cache.nextEvictionKey();
            assertThat(nxtKey).isEqualTo("aa");
            cache.put("dd", "4");
            assertThat(cache.get("aa")).isEmpty();
            assertThat(cache.get("dd")).isEqualTo("4");
        }
    }

    @Nested
    @DisplayName("Test heaviest Evicition policy")
    class TestHeaviestEvictionPolicy {
        private Cache cache;
        @BeforeEach
        void setUp() {
            cache = new CustomEvictionCache(2, new RemoveHeaviestEvistionPolicy());
        }

        @Test
        @DisplayName("Validate Eviction")
        void testEviction() {
            cache.put("az",   "v1");
            cache.put("by",  "v2");
            String nxtKey = cache.nextEvictionKey();
            assertThat(nxtKey).isEqualTo("az");
            cache.put("c", "v3");
            assertThat(cache.get("az")).isEmpty();
            assertThat(cache.get("by")).isEqualTo("v2");
            cache.put("by", "v9");
            assertThat(cache.get("by")).isEqualTo("v9");
            assertThat(cache.remove("by")).isTrue();
            assertThat(cache.remove("by")).isFalse();
            assertThat(cache.get("by")).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Test LRU Eviction policy")
    class TestLRUEvictionPolicy {
        private Cache cache;
        @BeforeEach
        void setUp() {
            cache = new CustomEvictionCache(2, new LRUEvictionPolicy());
        }

        @Test
        @DisplayName("Should evict the least recently used key")
        void testEviction() {
            cache.put("a", "v1");
            cache.put("b", "v2");
            String nxtKey = cache.nextEvictionKey();
            assertThat(nxtKey).isEqualTo("a");
            cache.put("c", "v3");
            assertThat(cache.get("a")).isEmpty();
            assertThat(cache.get("b")).isEqualTo("v2");
            assertThat(cache.get("c")).isEqualTo("v3");
        }

        @Test
        @DisplayName("Get should refresh recency and protect key from eviction")
        void testGetRefreshesRecency() {
            cache.put("a", "v1");
            cache.put("b", "v2");
            cache.get("a");
            cache.put("c", "v3");
            assertThat(cache.get("b")).isEmpty();
            assertThat(cache.get("a")).isEqualTo("v1");
            assertThat(cache.get("c")).isEqualTo("v3");
        }

        @Test
        @DisplayName("Put on existing key should refresh recency")
        void testPutRefreshesRecency() {
            cache.put("a", "v1");
            cache.put("b", "v2");
            cache.put("a", "v9");
            cache.put("c", "v3");
            assertThat(cache.get("b")).isEmpty();
            assertThat(cache.get("a")).isEqualTo("v9");
            assertThat(cache.get("c")).isEqualTo("v3");
        }
    }

    @Nested
    @DisplayName("Test LRU with DQ Eviction policy")
    class TestLRUDQEvictionPolicy {
        private Cache cache;
        @BeforeEach
        void setUp() {
            cache = new CustomEvictionCache(2, new LRUWithDQPolicy());
        }

        @Test
        @DisplayName("Should evict the least recently used key")
        void testEviction() {
            cache.put("a", "v1");
            cache.put("b", "v2");
            String nxtKey = cache.nextEvictionKey();
            assertThat(nxtKey).isEqualTo("a");
            cache.put("c", "v3");
            assertThat(cache.get("a")).isEmpty();
            assertThat(cache.get("b")).isEqualTo("v2");
            assertThat(cache.get("c")).isEqualTo("v3");
        }

        @Test
        @DisplayName("Get should refresh recency and protect key from eviction")
        void testGetRefreshesRecency() {
            cache.put("a", "v1");
            cache.put("b", "v2");
            cache.get("a");
            cache.put("c", "v3");
            assertThat(cache.get("b")).isEmpty();
            assertThat(cache.get("a")).isEqualTo("v1");
            assertThat(cache.get("c")).isEqualTo("v3");
        }

        @Test
        @DisplayName("Put on existing key should refresh recency")
        void testPutRefreshesRecency() {
            cache.put("a", "v1");
            cache.put("b", "v2");
            cache.put("a", "v9");
            cache.put("c", "v3");
            assertThat(cache.get("b")).isEmpty();
            assertThat(cache.get("a")).isEqualTo("v9");
            assertThat(cache.get("c")).isEqualTo("v3");
        }
    }

    @Nested
    @DisplayName("Test LFU Eviction policy")
    class TestLFUEvictionPolicy {
        private Cache cache;
        @BeforeEach
        void setUp() {
            cache = new CustomEvictionCache(2, new LFUEvictionPolicy());
        }

        @Test
        @DisplayName("Should evict the least frequently used key")
        void testEviction() {
            cache.put("a", "v1");
            cache.put("b", "v2");
            cache.get("b");
            cache.get("b");
            String nxtKey = cache.nextEvictionKey();
            assertThat(nxtKey).isEqualTo("a");
            cache.put("c", "v3");
            assertThat(cache.get("a")).isEmpty();
            assertThat(cache.get("b")).isEqualTo("v2");
            assertThat(cache.get("c")).isEqualTo("v3");
        }

        @Test
        @DisplayName("On tied frequency should evict the oldest key")
        void testTieBreaksOnOldest() {
            cache = new CustomEvictionCache(3, new LFUEvictionPolicy());
            cache.put("a", "v1");
            cache.put("b", "v2");
            cache.put("c", "v3");
            String nxtKey = cache.nextEvictionKey();
            assertThat(nxtKey).isEqualTo("a");
            cache.put("d", "v4");
            assertThat(cache.get("a")).isEmpty();
            assertThat(cache.get("b")).isEqualTo("v2");
            assertThat(cache.get("c")).isEqualTo("v3");
            assertThat(cache.get("d")).isEqualTo("v4");
        }

        @Test
        @DisplayName("Put on existing key should increment frequency")
        void testPutRefreshesFrequency() {
            cache.put("a", "v1");
            cache.put("b", "v2");
            cache.put("a", "v9");
            cache.put("c", "v3");
            assertThat(cache.get("b")).isEmpty();
            assertThat(cache.get("a")).isEqualTo("v9");
            assertThat(cache.get("c")).isEqualTo("v3");
        }
    }
}