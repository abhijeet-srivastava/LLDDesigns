package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomHashMap {
    private final double minLoadFactor;
    private final double maxLoadFactor;

    private Node[] buckets;
    private int size;

    /**
     * After each put() and remove(), check rehashing:
     * If LoadFactor is strictly greater than maxLoadFactor, grow by doubling buckets: 2 → 4 → 8 → ... until valid.
     * If LoadFactor is strictly less than minLoadFactor, shrink by halving buckets until valid, but never below 2.
     * Rehashing means: create new buckets and re-insert all existing entries using hash(key) % newBucketsCount.
     * @param minLoadFactor
     * @param maxLoadFactor
     */
    public CustomHashMap(double minLoadFactor, double maxLoadFactor) {
        this.minLoadFactor = minLoadFactor;
        this.maxLoadFactor = maxLoadFactor;
        buckets = new Node[2];
        size = 0;
    }

    /**
     * key and value length: 1 to 20
     * key contains only a-z
     * If key already exists, update its value (size does not increase).
     * @param key
     * @param value
     */
    void put(String key, String value){
        int idx = bucketIndex(key, buckets.length);
        Node curr = buckets[idx];
        while (curr != null) {
            if (curr.key.equals(key)) {
                curr.value = value;
                return;
            }
            curr = curr.next;
        }
        Node node = new Node(key, value);
        node.next = buckets[idx];
        buckets[idx] = node;
        size++;
        checkAndResize();
    }

    /**
     * returns value, or "" (empty string) if not found.
     * @param key
     * @return
     */
    String get(String key){
        int idx = bucketIndex(key, buckets.length);
        Node curr = buckets[idx];
        while (curr != null) {
            if (curr.key.equals(key)) {
                return curr.value;
            }
            curr = curr.next;
        }
        return "";
    }

    /**
     * If bucketIndex is invalid (<0 or ≥ bucketsCount) return empty list.
     * Return all keys currently in that bucket, sorted in lexicographically ascending order.
     * @param bucketIndex
     * @return
     */
    List<String> getBucketKeys(int bucketIndex){
        if (bucketIndex < 0 || bucketIndex >= buckets.length) {
            return List.of();
        }
        List<String> keys = new ArrayList<>();
        Node curr = buckets[bucketIndex];
        while (curr != null) {
            keys.add(curr.key);
            curr = curr.next;
        }
        Collections.sort(keys);
        return keys;
    }

    /**
     * Removes the key and returns its value, or "" (empty string) if not found.
     * @param key
     * @return
     */
    String remove(String key){
        int idx = bucketIndex(key, buckets.length);
        Node curr = buckets[idx];
        Node prev = null;
        while (curr != null) {
            if (curr.key.equals(key)) {
                if (prev == null) {
                    buckets[idx] = curr.next;
                } else {
                    prev.next = curr.next;
                }
                size--;
                checkAndResize();
                return curr.value;
            }
            prev = curr;
            curr = curr.next;
        }
        return "";
    }

    /**
     * number of entries currently stored.
     * @return
     */
    int size(){
        return size;
    }

    /**
     * current number of buckets.
     * @return
     */
    int bucketsCount(){
        return buckets.length;
    }

    /**
     * LoadFactor = round2(size / bucketsCount) where round2(x) = Math.round(x * 100.0) / 100.0
     * @return
     */
    private double loadFactor() {
        return round2((size * 1.0d) / (buckets.length * 1.0d));
    }

    private double round2(double x) {
        return Math.round(x * 100.0d) / 100.0d;
    }

    private int bucketIndex(String key, int bucketsLen) {
        return Math.floorMod(hash(key), bucketsLen);
    }

    private int hash(String key) {
        int n = key.length();
        int hash = n*n;
        for (char ch:key.toCharArray()) {
            if(Character.isLowerCase(ch)) {
                hash += (ch - 'a' + 1);
            } else if(Character.isUpperCase(ch)){
                hash += (ch - 'A' + 1);
            }
        }
        return hash;
    }

    private void checkAndResize() {
        double lf = loadFactor();
        if (lf > maxLoadFactor) {
            int newCount = buckets.length;
            do {
                newCount *= 2;
            } while (round2(size / (double) newCount) > maxLoadFactor);
            rehash(newCount);
        } else if (lf < minLoadFactor) {
            int newCount = buckets.length;
            while (newCount > 2 && round2(size / (double) newCount) < minLoadFactor) {
                newCount /= 2;
            }
            if (newCount != buckets.length) {
                rehash(newCount);
            }
        }
    }

    private void rehash(int newBucketCount) {
        Node[] newBuckets = new Node[newBucketCount];
        for (Node head : buckets) {
            Node curr = head;
            while (curr != null) {
                Node next = curr.next;
                int idx = bucketIndex(curr.key, newBucketCount);
                curr.next = newBuckets[idx];
                newBuckets[idx] = curr;
                curr = next;
            }
        }
        buckets = newBuckets;
    }

    private class Node {
        String key;
        String value;
        Node next;

        public Node(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}