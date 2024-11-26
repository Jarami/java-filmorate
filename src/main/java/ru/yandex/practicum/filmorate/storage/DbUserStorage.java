package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FailedToCreateEntity;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("db")
public class DbUserStorage extends NamedRepository<User> implements UserStorage {

    private static final String FIND_ALL_QUERY = """
        SELECT user_id as "user_id",
               user_name as "user_name",
               email as "email",
               login as "login",
               birthday as "birthday"
        FROM users""";

    private static final String FIND_BY_ID_QUERY = """
        SELECT * FROM users
        WHERE user_id = :id""";

    private static final String INSERT_QUERY = """
        INSERT INTO users(user_name, email, login, birthday)
        VALUES (:name, :email, :login, :birthday)""";

    private static final String UPDATE_QUERY = """
        UPDATE users SET user_name = :name, email = :email, login = :login, birthday = :birthday
        WHERE user_id = :id""";

    private static final String DELETE_QUERY = """
        DELETE FROM users
        WHERE user_id = :id""";

    private static final String DELETE_ALL_QUERY = """
        DELETE FROM users""";

    public DbUserStorage(NamedParameterJdbcTemplate namedTemplate, UserRowMapper mapper) {
        super(namedTemplate, mapper);
    }

    public List<User> getAll() {
        return super.getAll(FIND_ALL_QUERY);
    }

    public Optional<User> getById(Long userId) {
        return findOne(FIND_BY_ID_QUERY, Map.of("id", userId));
    }

    public User save(User user) {

        if (user.getId() == null) {
            KeyHolder keyHolder = insert(
                    INSERT_QUERY,
                    Map.of(
                            "name", user.getName(),
                            "email", user.getEmail(),
                            "login", user.getLogin(),
                            "birthday", user.getBirthday()),
                    new String[]{"user_id"}
            );
            Long id = keyHolder.getKeyAs(Long.class);
            if (id == null) {
                throw new FailedToCreateEntity("не удалось создать пользователя " + user);
            } else {
                log.debug("Пользователь {} сохранен с id = {}", user.getLogin(), user.getId());
                user.setId(id);
            }

        } else {

            update(UPDATE_QUERY,
                Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "login", user.getLogin(),
                    "birthday", user.getBirthday()));
        }

        return user;
    }

    public void delete(User user) {
        delete(DELETE_QUERY, Map.of("id", user.getId()));
    }

    public int deleteAll() {
        return delete(DELETE_ALL_QUERY);
    }
}
