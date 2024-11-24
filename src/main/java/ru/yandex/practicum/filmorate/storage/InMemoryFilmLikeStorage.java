package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Repository
public class InMemoryFilmLikeStorage implements FilmLikeStorage {

    private final Map<Long, Set<Long>> filmLikes;

    public InMemoryFilmLikeStorage() {
        filmLikes = new HashMap<>();
    }

    public boolean like(Film film, User user) {
        filmLikes.putIfAbsent(film.getId(), new HashSet<>());

        Set<Long> userIdLikedFilm = filmLikes.get(film.getId());
        if (!userIdLikedFilm.contains(user.getId())) {
            userIdLikedFilm.add(user.getId());
            //film.setRate(film.getRate() + 1);
            return true;
        }
        return false;
    }

    public boolean dislike(Film film, User user) {
        filmLikes.putIfAbsent(film.getId(), new HashSet<>());

        Set<Long> userIdLikedFilm = filmLikes.get(film.getId());
        if (!userIdLikedFilm.contains(user.getId())) {
            userIdLikedFilm.remove(user.getId());
            //film.setRate(film.getRate() - 1);
            return true;
        }
        return false;
    }
}
