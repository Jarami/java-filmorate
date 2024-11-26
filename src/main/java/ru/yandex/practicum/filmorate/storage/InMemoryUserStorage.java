package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

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
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getById(Long id) {
        return Optional.ofNullable(users.get(id));
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
