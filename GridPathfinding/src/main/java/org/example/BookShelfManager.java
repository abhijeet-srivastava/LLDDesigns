package org.example;

import java.util.ArrayList;
import java.util.List;

public class BookShelfManager {
    Node head;
    Node bookMark;

    public BookShelfManager() {
        this.head = new Node("dummy");
        this.bookMark = null;
    }

    void addBooks(int toIndex, List<String> books) {
        Node curr = head.next;
        Node prev = head;
        for(int i = 0; i < toIndex && curr != null; i++) {
            prev = curr;
            curr = curr.next;
        }
        for(String book: books) {
            Node node = new Node(book);
            prev.next = node;
            prev = node;
        }
        prev.next = curr;
    }
    void removeBooks(int fromIndex, int toIndex) {
        Node curr = head.next;
        Node prev = head;
        for(int i = 0; i < fromIndex && curr != null; i++) {
            prev = curr;
            curr = curr.next;
        }
        for(int i = fromIndex; i < toIndex && curr != null; i++) {
            curr = curr.next;
        }
        prev.next = curr;
    }
    void moveBooks(int fromIndex, int toIndex, int size) {
        if(size <= 0 || fromIndex == toIndex) {
            return;
        }
        Node fromPrev = head;
        for(int i = 0; i < fromIndex; i++) {
            fromPrev = fromPrev.next;
        }
        Node start = fromPrev.next;
        Node end = start;
        for(int i = 1; i < size; i++) {
            end = end.next;
        }
        Node afterSegment = end.next;
        fromPrev.next = afterSegment;

        Node toPrev = head;
        for(int i = 0; i < toIndex; i++) {
            toPrev = toPrev.next;
        }
        Node next = toPrev.next;

        toPrev.next = start;
        end.next = next;
    }

    List<String> getBooks() {
        List<String> res = new ArrayList<>();
        Node curr = head.next;
        while(curr != null) {
            res.add(curr.name);
            curr = curr.next;
        }
        return res;
    }
    void setBookmarkIndex(int index) {
        Node curr = head.next;
        for(int i = 0; i < index && curr != null; i++) {
            curr = curr.next;
        }
        this.bookMark = curr;
    }
    int getBookMarkIndex() {
        if(this.bookMark == null) {
            return -1;
        }
        int idx = 0;
        Node curr = head.next;
        while (curr != null && curr != this.bookMark) {
            curr = curr.next;
            idx += 1;
        }
        return curr == null ? -1 : idx;
    }

    public class Node {
        String name;
        Node next;

        public Node(String name) {
            this.name = name;
        }
    }
}
