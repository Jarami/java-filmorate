package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.validators.Marker;
import ru.yandex.practicum.filmorate.validators.UserLogin;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Data
@ToString
@EqualsAndHashCode(of = { "id" })
@Validated
@Slf4j
@Builder
@AllArgsConstructor
public class User {

    @Null(groups = Marker.OnCreate.class)
    @NotNull(groups = {Marker.OnUpdate.class, Marker.OnDelete.class})
    private Long id;

    @Email(groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String email;

    @UserLogin(
            message = "Логин должен быть заполнен и не должен содержать пробелов",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String login;

    private String name;

    @Past(groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private LocalDate birthday;

    private Set<Long> friendsId;

    public User() {
        this(null, null, null, null, null, new ArrayList<>());
    }

    public User(String email, String login, String name, LocalDate birthday) {
        this(null, email, login, name, birthday, new ArrayList<>());
    }

    public User(Long id, String email, String login, String name, LocalDate birthday, List<User> friends) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.friendsId = friends.stream().map(User::getId).collect(Collectors.toSet());
    }

    public void addFriend(User friend) {
        friendsId.add(friend.getId());
    }

    public void removeFriend(User friend) {
        friendsId.removeIf(friendId -> friend.getId().equals(friendId));
    }
}
