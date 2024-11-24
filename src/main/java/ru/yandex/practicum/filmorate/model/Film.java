package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.validators.After;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Validated
@ToString
@EqualsAndHashCode(of = { "id" })
@AllArgsConstructor
@Builder
public class Film {

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private FilmRating rating;
    private List<FilmGenre> genres;
    private Set<Long> likes;

    public Film() {
        this(null, null, null, null, 0, null, new ArrayList<>(), new HashSet<>());
    }

    public Film(Long id, String name, String description, String ratingName, LocalDate releaseDate, int duration) {
        this(id, name, description, releaseDate, duration, null, new ArrayList<>(), new HashSet<>());
    }

//    public Film(Long id, String name, String description, String ratingName, LocalDate releaseDate, int duration, Set<Long> likes) {
//        this(id, name, description, null, releaseDate, duration, likes);
//    }

    public int getLikeCount() {
        return likes.size();
    }

    public int addLike(long userId) {
        likes.add(userId);
        return likes.size();
    }

    public int removeLike(long userId) {
        likes.remove(userId);
        return likes.size();
    }
}
