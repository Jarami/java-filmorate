package ru.yandex.practicum.filmorate.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.util.TestUtil;

@Slf4j
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ DbUserStorage.class, UserRowMapper.class, DbFriendshipStorage.class, FriendshipRowMapper.class })
public class DbFriendshipStorageTest {

    private final DbUserStorage userStorage;
    private final DbFriendshipStorage friendshipStorage;

    private User user1;
    private User user2;
    private User user3;
    private User user4;

    @BeforeEach
    void setup() {
        shutdown();

        user1 = createUser();
        user2 = createUser();
        user3 = createUser();
        user4 = createUser();
    }

    @AfterEach
    void shutdown() {
        userStorage.deleteAll();
    }

    @Test
    void givenRequest_whenAdd_gotFriend() {
        boolean result = friendshipStorage.addFriend(user1, user2);

        assertTrue(result);
        List<Long> friendsId = friendshipStorage.getFriends(user1);
        assertEquals(1, friendsId.size());
        assertEquals(friendsId.get(0), user2.getId());
    }

    @Test
    void givenRepeatedRequest_whenAdd_gotNoChange() {
        friendshipStorage.addFriend(user1, user2);
        boolean result = friendshipStorage.addFriend(user1, user2);

        assertFalse(result);
        List<Long> friendsId = friendshipStorage.getFriends(user1);
        assertEquals(1, friendsId.size());
        assertEquals(friendsId.get(0), user2.getId());
    }

    @Test
    void givenResponse_whenAdd_gotAcceptedFriend() {
        friendshipStorage.addFriend(user1, user2);
        boolean result = friendshipStorage.addFriend(user2, user1);

        assertTrue(result);
        List<Long> friendsId1 = friendshipStorage.getFriends(user1);
        assertEquals(1, friendsId1.size());
        assertEquals(friendsId1.get(0), user2.getId());

        List<Long> friendsId2 = friendshipStorage.getFriends(user2);
        assertEquals(1, friendsId2.size());
        assertEquals(friendsId2.get(0), user1.getId());
    }

    @Test
    void givenRepeatedResponse_whenAdd_gotNoChange() {
        friendshipStorage.addFriend(user1, user2);
        friendshipStorage.addFriend(user2, user1);
        boolean result = friendshipStorage.addFriend(user2, user1);

        assertFalse(result);
        List<Long> friendsId1 = friendshipStorage.getFriends(user1);
        assertEquals(1, friendsId1.size());
        assertEquals(friendsId1.get(0), user2.getId());

        List<Long> friendsId2 = friendshipStorage.getFriends(user2);
        assertEquals(1, friendsId2.size());
        assertEquals(friendsId2.get(0), user1.getId());
    }

    @Test
    void testGetCommonFriends() {
        friendshipStorage.addFriend(user1, user2);
        friendshipStorage.addFriend(user1, user3);
        friendshipStorage.addFriend(user1, user4);

        friendshipStorage.addFriend(user2, user1);
        friendshipStorage.addFriend(user2, user3);
        friendshipStorage.addFriend(user2, user4);

        List<Long> commonFriendsId = friendshipStorage.getCommonFriends(user1, user2);
        Set<Long> expectedFriendsId = Set.of(user3.getId(), user4.getId());
        assertEquals(Set.copyOf(commonFriendsId), expectedFriendsId);
    }

    @Test
    void testRemoveFriend() {
        friendshipStorage.addFriend(user1, user2);
        friendshipStorage.removeFriend(user1, user2);

        List<Long> friendsId1 = friendshipStorage.getFriends(user1);
        assertTrue(friendsId1.isEmpty());
    }

    private User createUser() {
        return userStorage.save(TestUtil.getRandomUser());
    }
}
