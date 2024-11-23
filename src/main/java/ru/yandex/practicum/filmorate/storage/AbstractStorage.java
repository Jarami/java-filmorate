package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;
import java.util.Optional;

public interface AbstractStorage<K, T> {
    T save(T t);

    Collection<T> getAll();

    Optional<T> getById(K id);

    void delete(T t);

    int deleteAll();
}
