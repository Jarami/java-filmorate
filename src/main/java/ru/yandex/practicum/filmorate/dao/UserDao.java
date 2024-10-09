package ru.yandex.practicum.filmorate.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.model.User;

@Repository
public class UserDao implements EntityDao<Integer, User> {

    private static int id = 1;
    private final Map<Integer, User> users = new HashMap<>();

    public void save(User user) {
        if (user.getId() == null) {
            user.setId(generateId());
        }
        users.put(user.getId(), user);
    }

    public Collection<User> getAll() {
        return users.values();
    }

    public User getById(Integer id) {
        return users.get(id);
    }

    public void delete(User user) {
        users.remove(user.getId());
    }

    private int generateId() {
        return id++;
    }
}
