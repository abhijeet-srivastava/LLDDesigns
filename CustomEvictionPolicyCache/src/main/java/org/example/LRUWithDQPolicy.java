package org.example;

import java.util.HashMap;
import java.util.Map;

public class LRUWithDQPolicy implements EvictionPolicy {

    Map<String, Node> cache;
    Node head;
    Node tail;

    public LRUWithDQPolicy() {
        this.cache = new HashMap<>();
    }

    private class Node {
        String key;
        Node next;
        Node prev;

        public Node(String key) {
            this.key = key;
        }
    }

    @Override
    public String nextEvictionKey() {
        if(this.tail == null) {
            return "";
        }
        return this.tail.key;
    }

    @Override
    public void addKey(String key) {
        Node node = new Node(key);
        cache.put(key, node);
        if(this.head == null) {
            this.head = node;
            this.tail = node;
        } else {
            head.next = node;
            node.prev = head;
            this.head = node;
        }
    }

    @Override
    public void removeKey(String key) {
        Node node = cache.get(key);
        cache.remove(key);
        if(node == null) {
            return;
        } else if(node == head && node == tail) {
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
            next.prev = prev;
            prev.next = node;
        }
    }

    @Override
    public void touchKey(String key) {
        Node node = cache.get(key);
        if(node == head) {
            return;
        }else if(node == tail) {
            Node next = tail.next;
            next.prev = next;
            tail = next;

            head.next = node;
            node.prev = head;
            head = node;
        } else {
            Node next  = node.next;
            Node prev = node.prev;
            next.prev = prev;
            prev.next = next;

            head.next = node;
            node.prev = head;
            node.next = next;
            head = node;
        }
    }
}
