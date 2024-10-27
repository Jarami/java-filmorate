package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    User save(User user);

    Collection<User> getAll();

    User update(User user);

    void delete(User user);
}
