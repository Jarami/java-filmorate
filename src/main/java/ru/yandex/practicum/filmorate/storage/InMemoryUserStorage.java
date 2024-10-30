package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryUserStorage implements UserStorage {

    private static long id = 1L;
    private final Map<Long, User> users = new HashMap<>();

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
    public User getById(Long id) {
        return users.get(id);
    }

    @Override
    public void delete(User user) {
        users.remove(user.getId());
    }

    @Override
    public int deleteAll() {
        int userCount = users.size();
        users.clear();
        return userCount;
    }

    private long generateId() {
        return id++;
    }
}
