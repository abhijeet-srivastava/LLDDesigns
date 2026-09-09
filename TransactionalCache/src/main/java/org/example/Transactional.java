package org.example;

public interface Transactional {
    int transaction();

    void commit();

    void rollback();
}
