package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import lombok.*;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.validators.Marker;
import ru.yandex.practicum.filmorate.validators.UserLogin;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User.
 */
@Data
@ToString
@EqualsAndHashCode(of = { "id" })
@Validated
public class User {

    @Null(groups = Marker.OnCreate.class)
    @NotNull(groups = {Marker.OnUpdate.class, Marker.OnDelete.class})
    private Integer id;

    @Email(groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String email;

    @UserLogin(message = "Логин должен быть заполнен и не должен содержать пробелов", groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String login;

    private String name;

    @Past(groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private LocalDate birthday;

    private Set<Integer> friendsId = new HashSet<>();

    public User() {
        this(null, null, null, null, null, new ArrayList<>());
    }

    public User(String email, String login, String name, LocalDate birthday) {
        this(null, email, login, name, birthday, new ArrayList<>());
    }

    public User(Integer id, String email, String login, String name, LocalDate birthday, List<User> friends) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.friendsId = friends.stream().map(User::getId).collect(Collectors.toSet());
    }
}
