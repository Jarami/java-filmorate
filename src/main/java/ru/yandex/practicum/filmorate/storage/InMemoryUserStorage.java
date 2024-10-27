package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.UserNotFound;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryUserStorage implements UserStorage {

    private static int id = 1;
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(generateId());
        }
        users.put(user.getId(), user);

        return user;
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new UserNotFound(user);
        }

        users.put(user.getId(), user);

        return user;
    }

    @Override
    public void delete(User user) {
        if (user.getId() == null) {
            throw new UserNotFound(user);
        }

        users.remove(user.getId());
    }

    private int generateId() {
        return id++;
    }
}
