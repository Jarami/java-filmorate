package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Qualifier("db")
public class DbUserStorage extends BaseRepository<User> implements UserStorage {

    private static final String FIND_ALL_QUERY = """
        SELECT user_id as "user_id",
               user_name as "user_name",
               email as "email",
               login as "login",
               birthday as "birthday"
        FROM users""";

    private static final String FIND_BY_ID_QUERY = """
        SELECT * FROM users
        WHERE user_id = ?""";

    private static final String INSERT_QUERY = """
        INSERT INTO users(user_name, email, login, birthday)
        VALUES (?, ?, ?, ?)""";

    private static final String UPDATE_QUERY = """
        UPDATE users SET user_name = ?, email = ?, login = ?, birthday = ?
        WHERE user_id = ?""";

    private static final String DELETE_QUERY = """
        DELETE FROM users
        WHERE user_id = ?""";

    private static final String DELETE_ALL_QUERY = """
        DELETE FROM users""";

    private static final String GET_FRIENDS_ID = """
        SELECT receiving_user_id as "friend_id"
        FROM friendship
        WHERE sending_user_id = ? AND status = 'accepted'

        UNION

        SELECT sending_user_id as "friend_id"
        FROM friendship
        WHERE receiving_user_id = ? AND status = 'accepted'""";

    public DbUserStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<User> getAll() {
        List<User> users = findMany(FIND_ALL_QUERY);

        users.forEach(user -> {
            List<Long> friendsId = jdbc.queryForList(GET_FRIENDS_ID, Long.class, user.getId(), user.getId());
            user.setFriendsId(Set.copyOf(friendsId));
        });

        return users;
    }

    public Optional<User> getById(Long userId) {

        Optional<User> user = findOne(FIND_BY_ID_QUERY, userId);

        user.ifPresent(u -> {
            List<Long> friendsId = jdbc.queryForList(GET_FRIENDS_ID, Long.class, u.getId(), u.getId());
            u.setFriendsId(Set.copyOf(friendsId));
        });

        return user;
    }

    public User save(User user) {

        if (user.getId() == null) {
            Number id = insert(
                    INSERT_QUERY,
                    user.getName(),
                    user.getEmail(),
                    user.getLogin(),
                    user.getBirthday()
            );
            user.setId((long)id);

        } else {
            update(
                    UPDATE_QUERY,
                    user.getName(),
                    user.getEmail(),
                    user.getLogin(),
                    user.getBirthday(),
                    user.getId()
            );
        }

        return user;
    }

    public void delete(User user) {
        delete(DELETE_QUERY, user.getId());
    }

    public int deleteAll() {
        return executeUpdate(DELETE_ALL_QUERY);
    }
}
