package org.example;

public class Main {
    public static void main(String[] args) {
        Cache cache = new TransactionalCache();

        cache.put("name", "Abhijeet");
        System.out.println("name = " + cache.get("name"));

        int txn1 = cache.transaction();
        cache.put("name", "Changed in txn1");
        System.out.println("name inside txn1 = " + cache.get("name"));

        int txn2 = cache.transaction();
        cache.delete("name");
        System.out.println("name inside txn2 (after delete) = " + cache.get("name"));

        cache.rollback();
        System.out.println("name after rollback of txn2 = " + cache.get("name"));

        cache.commit();
        System.out.println("name after commit of txn1 = " + cache.get("name"));

        try {
            cache.commit();
        } catch (IllegalStateException e) {
            System.out.println("Expected error: " + e.getMessage());
        }
    }
}