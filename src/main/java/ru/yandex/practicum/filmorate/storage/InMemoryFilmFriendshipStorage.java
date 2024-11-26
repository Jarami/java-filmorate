package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static ru.yandex.practicum.filmorate.model.FriendshipStatus.ACCEPTED;
import static ru.yandex.practicum.filmorate.model.FriendshipStatus.PENDING;

@Slf4j
@Repository
public class InMemoryFilmFriendshipStorage implements FriendshipStorage {

    private final List<Friendship> friendships;

    public InMemoryFilmFriendshipStorage() {
        friendships = new ArrayList<>();
    }

    private void init(User user) {
//        friends.putIfAbsent(user.getId(), new HashSet<>());
    }

    @Override
    public List<Long> getFriends(User user) {

        Stream<Long> receivingIds = friendships.stream()
                .filter(f -> f.getSendingUserId().equals(user.getId()) && f.getStatus().equals(ACCEPTED))
                .map(Friendship::getReceivingUserId);

        Stream<Long> sendingIds = friendships.stream()
                .filter(f -> f.getSendingUserId().equals(user.getId()) && (f.getStatus().equals(ACCEPTED) || f.getStatus().equals(PENDING)))
                .map(Friendship::getSendingUserId);

        return Stream.concat(receivingIds, sendingIds).toList();
    }

    @Override
    public List<Long> getCommonFriends(User user1, User user2) {

        List<Long> commonFriends = new ArrayList<>(getFriends(user1));
        commonFriends.retainAll(getFriends(user2));

        return commonFriends;
    }

    @Override
    public boolean addFriend(User user, User friend) {

        // Запрос дружбы, посланный пользователем user пользователю friend
        Friendship friendshipRequest = getFriendshipForUsers(user, friend);

        // Это повтор запроса дружбы => игнорируем
        if (friendshipRequest != null) {
            return false;
        }

        // Запрос дружбы, посланный пользователем friend пользователю user
        // Если такой существует, то это значит, что сейчас мы подтверждаем запрос
        Friendship friendshipResponse = getFriendshipForUsers(friend, user);

        // Это повтор принятия запроса на дружбу => игнорируем
        if (friendshipResponse != null && friendshipResponse.isAccepted()) {
            return false;
        }

        // Это подтверждение запроса на дружбу => меняем статус на accepted
        if (friendshipResponse != null) {
            // user и friend поменяны местами, ведь на самом деле это friend
            // отправил запрос, а user его подтверждает
            if (acceptFriendship(friend, user)) {
                log.info("friendship request {}->{} accepted", friend.getLogin(), user.getLogin());
                return true;
            } else {
                log.error("friendship request {}->{} acceptance failed", friend.getLogin(), user.getLogin());
                return false;
            }
        }

        Friendship friendship = Friendship.builder()
                .sendingUserId(friend.getId())
                .receivingUserId(user.getId())
                .status(PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        friendships.add(friendship);

        return true;
    }

    @Override
    public boolean removeFriend(User user, User friend) {

        boolean result = friendships.removeIf(f -> f.getSendingUserId().equals(user.getId()) &&
                f.getReceivingUserId().equals(friend.getId()));

        if (result) {
            return true;
        }
        return false;

//        result = friendships.removeIf(f -> f.getReceivingUserId().equals(friend.getId()) &&
//                    f.getSendingUserId().equals(user.getId()));
//
//        return result;
    }

    private Friendship getFriendshipForUsers(User user, User friend) {
        return friendships.stream()
                .filter(f -> f.getSendingUserId().equals(user.getId()) && f.getReceivingUserId().equals(friend.getId()))
                .findFirst()
                .orElse(null);
    }

    private boolean acceptFriendship(User acceptor, User sender) {

        Friendship friendshipResponse = getFriendshipForUsers(sender, acceptor);

        friendshipResponse.setStatus(ACCEPTED);
        friendshipResponse.setAcceptedAt(LocalDateTime.now());

        return true;
    }
}
