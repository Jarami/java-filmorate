package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@ToString
@Validated
@Slf4j
@Builder
@AllArgsConstructor
public class User {

    private Long id;
    private String email;
    private String login;

    private String name;
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
}
