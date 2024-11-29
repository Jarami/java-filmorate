package ru.yandex.practicum.filmorate.storage;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.FriendshipRowMapper;

@Slf4j
@Repository
@Qualifier("db")
public class DbFriendshipStorage extends NamedRepository<Friendship> implements FriendshipStorage {

    private static final String FIND_FRIENDSHIP = """
       SELECT *
       FROM friendship
       WHERE user_id = :userId AND friend_id = :friendId""";

    private static final String FIND_FRIENDS_ID = """
        SELECT friend_id
        FROM friendship
        WHERE user_id = :userId

        UNION

        SELECT user_id
        FROM friendship
        WHERE friend_id = :userId AND status = 'accepted'""";

    private static final String ACCEPT_FRIENDSHIP_QUERY = """
        UPDATE friendship
        SET status = 'accepted', accepted_at = :acceptedAt
        WHERE friend_id = :friendId AND user_id = :userId""";

    private static final String COMMON_FRIENDS_QUERY = """
        (SELECT friend_id
        FROM friendship
        WHERE user_id = :id1

        UNION

        SELECT user_id
        FROM friendship
        WHERE friend_id = :id1 AND status = 'accepted')

        INTERSECT

        (SELECT friend_id
        FROM friendship
        WHERE user_id = :id2

        UNION

        SELECT user_id
        FROM friendship
        WHERE friend_id = :id2 AND status = 'accepted')""";

    private static final String INSERT_FRIENDSHIP_REQUEST = """
        INSERT INTO friendship (friend_id, user_id, status, requested_at)
        VALUES (:friendId, :userId, :status, :requestedAt)""";

    private static final String DELETE_QUERY = """
        DELETE FROM friendship
        WHERE friend_id = :friendId AND user_id = :userId""";

    protected final NamedParameterJdbcTemplate namedTemplate;

    public DbFriendshipStorage(NamedParameterJdbcTemplate namedTemplate, FriendshipRowMapper mapper) {
        super(namedTemplate, mapper);
        this.namedTemplate = namedTemplate;
    }

    @Override
    public List<Long> getFriends(User user) {
        return namedTemplate.queryForList(FIND_FRIENDS_ID, Map.of("userId", user.getId()), Long.class);
    }

    @Override
    public List<Long> getCommonFriends(User user1, User user2) {

        log.debug("get common friends: {} and {}", user1, user2);

        return namedTemplate.queryForList(COMMON_FRIENDS_QUERY,
                Map.of("id1", user1.getId(), "id2", user2.getId()), Long.class);
    }

    @Override
    public boolean addFriend(User user, User friend) {

        log.debug("add friend: {} and {}", user, friend);

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        // Запрос дружбы, посланный пользователем user пользователю friend
        Friendship friendshipRequest = getFriendshipForUsers(user, friend).orElse(null);

        // Это повтор запроса дружбы => игнорируем
        if (friendshipRequest != null) {
            log.debug("friendship request {}->{} already exists", user.getLogin(), friend.getLogin());
            return false;
        }

        // Запрос дружбы, посланный пользователем friend пользователю user
        // Если такой существует, то это значит, что сейчас мы подтверждаем запрос
        Friendship friendshipResponse = getFriendshipForUsers(friend, user).orElse(null);

        // Это повтор принятия запроса на дружбу => игнорируем
        if (friendshipResponse != null && friendshipResponse.isAccepted()) {
            log.debug("friendship request {}->{} already accepted", friend.getLogin(), user.getLogin());
            return false;
        }

        // Это принятие заявки на дружбу => меняем статус на accepted
        if (friendshipResponse != null) {

            log.debug("accepting friendship request {}->{}", friend.getLogin(), user.getLogin());

            // user и friend поменяны местами, ведь на самом деле это friend
            // отправил запрос, а user его подтверждает
            if (acceptFriendship(friend, user)) {
                log.info("accepting friendship request {}->{} done", friend.getLogin(), user.getLogin());
                return true;
            } else {
                log.error("accepting friendship request {}->{} failed", friend.getLogin(), user.getLogin());
                return false;
            }
        }

        // сейчас мы точно знаем, что friendshipRequest == null и friendshipResponse == null
        Map<String, Object> params = Map.of("userId", user.getId(), "friendId", friend.getId(),
                "status", "pending", "requestedAt", now);

        KeyHolder keyHolder = insert(INSERT_FRIENDSHIP_REQUEST, params, new String[]{"friendship_id"});
        Long id = keyHolder.getKeyAs(Long.class);

        if (id == null) {
            log.error("creating friendship request {}->{} failed", user.getLogin(), friend.getLogin());
            return false;
        } else {
            log.info("creating friendship request {}->{} done ({})", user.getLogin(), friend.getLogin(), id);
            return true;
        }
    }

    @Override
    public boolean removeFriend(User user, User friend) {

        log.debug("deleting friendship {}->{}", user.getLogin(), friend.getLogin());

        int rowsUpdated = update(DELETE_QUERY,
                Map.of("userId", user.getId(), "friendId", friend.getId()));

        if (rowsUpdated > 0) {
            log.debug("deleting friendship {}->{} done", friend.getLogin(), user.getLogin());
        }

        return rowsUpdated > 0;
    }

    private Optional<Friendship> getFriendshipForUsers(User user, User friend) {
        return findOne(FIND_FRIENDSHIP, Map.of("userId", user.getId(), "friendId", friend.getId()));
    }

    private boolean acceptFriendship(User user, User friend) {
        Map<String, Object> params = Map.of("userId", user.getId(), "friendId", friend.getId(),
                "acceptedAt", LocalDateTime.now());

        int rowsUpdated = update(ACCEPT_FRIENDSHIP_QUERY, params);
        return rowsUpdated > 0;
    }
}
