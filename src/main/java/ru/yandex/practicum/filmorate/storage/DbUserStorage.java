package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.util.List;
import java.util.Optional;

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

    public DbUserStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<User> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<User> getById(Long UserId) {
        return findOne(FIND_BY_ID_QUERY, UserId);
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
