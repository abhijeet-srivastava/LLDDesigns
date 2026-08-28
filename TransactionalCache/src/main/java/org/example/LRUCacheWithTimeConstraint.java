package org.example;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LRUCacheWithTimeConstraint {

    Map<String, Node> cache;
    Node head;
    Node tail;
    int capacity;
    int ttl;

    public LRUCacheWithTimeConstraint(int capacity, int ttlSeconds) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.cache = new HashMap<>();
        this.capacity = capacity;
        ttl = ttlSeconds;
    }

    public String get(String key, int currentTime) {
        Node node = getNode(key, currentTime);
        if(node == null || isExpired(node, currentTime)) {
            return "NOT_FOUND";
        }
        return node.value;
    }

    private Node getNode(String key, int currTime) {
        if(!cache.containsKey(key) || isExpired(cache.get(key), currTime)) {
            return null;
        }
        Node node = cache.get(key);
        moveToFront(node);
        return node;
    }

    private void moveToFront(Node node) {
        if(node == head) {
            return;
        }
        removeNode(node);
        addToFront(node);
    }

    private void addToFront(Node node) {
        if(head == null) {
            head = node;
            tail = node;
            return;
        }
        head.next = node;
        node.prev = head;
        head = node;
    }

    private void removeNode(Node node) {
        if(node == head && node == tail) {
            head = null;
            tail = null;
        } else if(node == head) {
            Node prev = head.prev;
            prev.next = null;
            head = prev;
        } else if(node == tail) {
            Node next = tail.next;
            next.prev = null;
            tail = next;
        } else {
            Node next = node.next;
            Node prev = node.prev;
            prev.next = next;
            next.prev = prev;
        }
    }

    public void put(String key, String value,  int currentTime) {
        /*Iterator<Map.Entry<String, Node>> itr = cache.entrySet().iterator();
        while (itr.hasNext()) {
            var entry = itr.next();
            if(isExpired(entry.getValue(), currentTime)) {
                removeNode(entry.getValue());
                itr.remove();
            }
        }*/
        Node curr = head;
        while(curr != null && isExpired(curr, currentTime)) {
            cache.remove(curr.key);
            removeNode(curr);
            curr = curr.next;
        }
        Node node = getNode(key, currentTime);
        if(node != null) {
            node.value = value;
            node.expiresAt = currentTime + ttl;
            return;
        }
        node = new Node(key, value, currentTime+ttl);
        cache.put(key, node);
        addToFront(node);
        if(cache.size() > capacity) {
            Node tail = popTail();
            cache.remove(tail.key);
        }

    }

    private Node popTail() {
        Node currTail = tail;
        tail = tail.next;
        tail.prev = null;
        return currTail;
    }

    private boolean isExpired(Node node, int currTime) {
        return node.expiresAt <= currTime;
    }


    private class Node {
        String key;
        String value;
        int expiresAt;
        Node next;
        Node prev;

        public Node(String key, String value, int expiresAt) {
            this.key = key;
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }
}
