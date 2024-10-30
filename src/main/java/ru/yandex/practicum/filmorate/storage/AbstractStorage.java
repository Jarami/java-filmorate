package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;

public interface AbstractStorage<K, T> {
    T save(T t);

    Collection<T> getAll();

    T getById(K id);

    void delete(T t);

    int deleteAll();
}
