package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.validators.After;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Validated
@ToString
@EqualsAndHashCode(of = { "id" })
@AllArgsConstructor
@Builder
public class Film {
    @Null(groups = Marker.OnCreate.class)
    @NotNull(groups = {Marker.OnUpdate.class, Marker.OnDelete.class})
    private Long id;

    @NotBlank(
            message = "Название фильма не должно быть пустым",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String title;

    @Size(
            max = 200,
            message = "Описание фильма не должно быть больше, чем 200 символов",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String description;

    @NotNull
    // @Pattern(regexp = "G|PG|PG-13|R|NC-17")
    private FilmRating rating;

    @After(
            after = "1895-12-28",
            message = "Релиз фильма не должен быть раньше {after}",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private LocalDate releaseDate;

    @Positive(groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private int duration;

    private Set<Long> likes;

    public Film() {
        this(null, null, null, FilmRating.G, null, 0, new HashSet<>());
    }

    public Film(Long id, String name, String description, String ratingName, LocalDate releaseDate, int duration) {
        this(id, name, description, ratingName, releaseDate, duration, new HashSet<>());
    }

    public Film(Long id, String name, String description, String ratingName, LocalDate releaseDate, int duration, Set<Long> likes) {
        this(id, name, description, FilmRating.getByName(ratingName), releaseDate, duration, likes);
    }

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
