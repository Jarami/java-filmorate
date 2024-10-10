package ru.yandex.practicum.filmorate.dao;

import java.util.Collection;

public interface EntityDao<K, T> {

    T getById(K id);

    Collection<T> getAll();

    void save(T t);

    void delete(T t);

    int deleteAll();
}
