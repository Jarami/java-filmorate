package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FailedToCreateEntity;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("db")
public class DbFilmStorage extends NamedRepository<Film> implements FilmStorage {

    private static final String FIND_ALL_QUERY = """
            SELECT f.film_id as "film_id",
                   f.film_name as "film_name",
                   f.description as "description",
                   f.release_date as "release_date",
                   f.duration as "duration",
                   fr.mpa_id as "mpa_id",
                   fr.mpa_name as "mpa_name",
                   count(fl.film_id) as "rate"
            FROM films f
            INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
            LEFT JOIN film_likes fl ON fl.film_id = f.film_id
            GROUP BY f.film_id, fr.mpa_id""";

    private static final String FIND_TOP_QUERY = """
            SELECT f.film_id as "film_id",
                   f.film_name as "film_name",
                   f.description as "description",
                   f.release_date as "release_date",
                   f.duration as "duration",
                   fr.mpa_id as "mpa_id",
                   fr.mpa_name as "mpa_name",
                   count(fl.film_id) as "rate"
            FROM films f
            INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
            LEFT JOIN film_likes fl ON fl.film_id = f.film_id
            GROUP BY f.film_id, fr.mpa_id
            ORDER BY count(fl.film_id) desc
            LIMIT :count""";

    private static final String FIND_RECOMMENDATIONS_QUERY = """
            SELECT f.film_id AS "film_id",
                   f.film_name AS "film_name",
                   f.description AS "description",
                   f.release_date AS "release_date",
                   f.duration AS "duration",
                   fr.mpa_id AS "mpa_id",
                   fr.mpa_name AS "mpa_name",
                   count(fl.film_id) AS "rate"
            FROM films f
            INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
            LEFT JOIN film_likes fl ON fl.film_id = f.film_id
            WHERE f.film_id IN (
                SELECT film_id
                FROM film_likes fl
                WHERE user_id IN (
                    SELECT fl.user_id
                    FROM film_likes fl
                    INNER JOIN film_likes fl2 ON fl.film_id = fl2.film_id
                    WHERE fl.user_id <> :userId AND fl2.user_id = :userId
                    GROUP BY fl.user_id
                    ORDER BY count(fl.film_id) DESC
                    LIMIT 1
                )

                EXCEPT

                SELECT film_id
                FROM film_likes fl
                WHERE user_id = :userId
            )
            GROUP BY f.film_id, fr.mpa_id
            """;

    private static final String FIND_COMMON_FILMS = """
            SELECT f.film_id as "film_id",
                   f.film_name as "film_name",
                   f.description as "description",
                   f.release_date as "release_date",
                   f.duration as "duration",
                   fr.mpa_id as "mpa_id",
                   fr.mpa_name as "mpa_name",
                   count(fl.film_id) as "rate"
            FROM films f
            LEFT JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
            LEFT JOIN film_likes fl ON fl.film_id = f.film_id
            WHERE f.film_id IN (
                SELECT fl1.film_id
                FROM film_likes fl1
                INNER JOIN film_likes fl2 ON fl1.film_id = fl2.film_id
                WHERE fl1.user_id = :userId AND fl2.user_id = :friendId
            )
            GROUP BY f.film_id, fr.mpa_id
            ORDER BY count(fl.film_id) desc
            """;

    private static final String FIND_TOP_YEAR = """
            SELECT  f.film_id as film_id,
                    f.film_name as film_name,
                    f.description as description,
                    f.release_date as release_date,
                    f.duration as duration,
                    fr.mpa_id as mpa_id,
                    fr.mpa_name as mpa_name,
                    count(fl.film_id) as rate
            FROM films f
            INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
            LEFT JOIN film_likes fl ON fl.film_id = f.film_id
            WHERE EXTRACT(YEAR FROM f.release_date) = :year
            GROUP BY f.film_id, fr.mpa_id
            ORDER BY count(fl.film_id) desc
            LIMIT :count""";

    private static final String FIND_TOP_GENRE = """
            SELECT  f.film_id as film_id,
                    f.film_name as film_name,
                    f.description as description,
                    f.release_date as release_date,
                    f.duration as duration,
                    fr.mpa_id as mpa_id,
                    fr.mpa_name as mpa_name,
                    COUNT(fl.film_id) as rate
            FROM films f
            INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
            LEFT JOIN film_likes fl ON fl.film_id = f.film_id
            WHERE f.film_id IN (
                SELECT FILM_ID FROM FILMS_GENRES_RELATION fgr WHERE GENRE_ID = :genre_id
                )
            GROUP BY f.film_id, fr.mpa_id
            ORDER BY count(fl.film_id) desc
            LIMIT :count""";


    private static final String FIND_TOP_YEAR_GENRE = """
            SELECT  f.film_id as film_id,
                    f.film_name as film_name,
                    f.description as description,
                    f.release_date as release_date,
                    f.duration as duration,
                    fr.mpa_id as mpa_id,
                    fr.mpa_name as mpa_name,
                    COUNT(fl.film_id) as rate
            FROM films f
            INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
            LEFT JOIN film_likes fl ON fl.film_id = f.film_id
            WHERE EXTRACT(YEAR FROM f.release_date) = :year
            AND f.film_id IN (
                SELECT FILM_ID FROM FILMS_GENRES_RELATION fgr WHERE GENRE_ID = :genre_id
                )
            GROUP BY f.film_id, fr.mpa_id
            ORDER BY count(fl.film_id) desc
            LIMIT :count""";

    private static final String FIND_BY_ID_QUERY = """
        SELECT f.film_id as "film_id",
               f.film_name as "film_name",
               f.description as "description",
               f.release_date as "release_date",
               f.duration as "duration",
               fr.mpa_id as "mpa_id",
               fr.mpa_name as "mpa_name",
               count(fl.film_id) as "rate"
        FROM films f
        INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
        LEFT JOIN film_likes fl ON fl.film_id = f.film_id
        WHERE f.film_id = :filmId
        GROUP BY f.film_id, fr.mpa_id""";

    private static final String FIND_FILM_GENRES_QUERY = """
        SELECT g.genre_id as "id",
               g.genre_name as "name"
        FROM film_genres g
        INNER JOIN films_genres_relation r ON g.genre_id = r.genre_id
        WHERE r.film_id = :filmId
        ORDER BY g.genre_id""";

    private static final String FIND_FILMS_BY_TITLE_QUERY = """
        SELECT f.film_id as "film_id",
                f.film_name as "film_name",
                f.description as "description",
                f.release_date as "release_date",
                f.duration as "duration",
                fr.mpa_id as "mpa_id",
                fr.mpa_name as "mpa_name",
                COUNT(DISTINCT fl.film_id) as "rate"
         FROM films AS f
         LEFT OUTER JOIN film_likes AS fl ON fl.film_id = f.film_id
         LEFT OUTER JOIN film_mpa AS fr ON f.mpa_id = fr.mpa_id
         WHERE LOWER(f.film_name) LIKE :name
         GROUP BY f.film_id, fr.mpa_id
         ORDER BY COUNT(DISTINCT fl.film_id)
        """;

    private static final String FIND_FILMS_BY_DIRECTOR_QUERY = """
        SELECT f.film_id as "film_id",
                f.film_name as "film_name",
                f.description as "description",
                f.release_date as "release_date",
                f.duration as "duration",
                fr.mpa_id as "mpa_id",
                fr.mpa_name as "mpa_name",
                COUNT(DISTINCT fl.film_id) as "rate"
         FROM films AS f
         LEFT OUTER JOIN film_likes AS fl ON fl.film_id = f.film_id
         LEFT OUTER JOIN film_mpa AS fr ON f.mpa_id = fr.mpa_id
         LEFT OUTER JOIN films_directors AS fd ON f.film_id = fd.film_id
         LEFT OUTER JOIN directors AS d ON fd.director_id = d.director_id
         WHERE LOWER(d.name) LIKE :name
         GROUP BY f.film_id, fr.mpa_id
         ORDER BY COUNT(DISTINCT fl.film_id)
        """;

    private static final String FIND_FILMS_BY_FILM_AND_DIRECTOR_QUERY = """
         SELECT f.film_id as "film_id",
                f.film_name as "film_name",
                f.description as "description",
                f.release_date as "release_date",
                f.duration as "duration",
                fr.mpa_id as "mpa_id",
                fr.mpa_name as "mpa_name",
                COUNT(DISTINCT fl.film_id) as "rate"
         FROM films AS f
         LEFT OUTER JOIN film_likes AS fl ON fl.film_id = f.film_id
         LEFT OUTER JOIN film_mpa AS fr ON f.mpa_id = fr.mpa_id
         LEFT OUTER JOIN films_directors AS fd ON f.film_id = fd.film_id
         LEFT OUTER JOIN directors AS d ON fd.director_id = d.director_id
         WHERE LOWER(d.name) LIKE :name OR LOWER(f.film_name) LIKE :name
         GROUP BY f.film_id, fr.mpa_id
         ORDER BY COUNT(DISTINCT fl.film_id)
        """;

    private static final String FIND_FILM_DIRECTORS_QUERY = """
        SELECT d.director_id as "director_id",
               d.name as "name"
        FROM directors d
        INNER JOIN films_directors fd ON fd.director_id = d.director_id
        WHERE fd.film_id = :filmId""";

    private static final String INSERT_QUERY = """
        INSERT INTO films(film_name, description, release_date, duration, mpa_id)
        VALUES (:name, :description, :releaseDate, :duration, :mpaId)""";

    private static final String UPDATE_QUERY = """
        UPDATE films
        SET film_name = :name,
            description = :description,
            release_date = :releaseDate,
            duration = :duration,
            mpa_id = :mpaId
        WHERE film_id = :filmId""";

    private static final String DELETE_QUERY = """
        DELETE FROM films
        WHERE film_id = :filmId""";

    private static final String DELETE_ALL_QUERY = """
        DELETE FROM films""";

    private static final String INSERT_FILM_GENRE_REL_QUERY = """
        INSERT INTO films_genres_relation (film_id, genre_id)
        VALUES (:filmId, :genreId)""";

    private static final String DELETE_FILM_GENRE_REL_QUERY = """
        DELETE FROM films_genres_relation
        WHERE film_id = :filmId""";

    private static final String INSERT_FILM_DIRECTORS_QUERY = """
        INSERT INTO films_directors (film_id, director_id)
        VALUES (:filmId, :directorId)""";

    private static final String DELETE_FILM_DIRECTORS_QUERY = """
        DELETE FROM films_directors
        WHERE film_id = :filmId""";

    private static final String SELECT_SORTED_DIRECTOR_FILM_BY_RATE = """
             SELECT f.film_id as film_id,
             		f.film_name as film_name,
            		f.description as description,
            		f.release_date as "release_date",
            		f.duration as duration,
            		fr.mpa_id as mpa_id,
            		fr.mpa_name as mpa_name,
            		count(fl.film_id) as "rate"
            FROM films f
            INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
            LEFT JOIN film_likes fl ON fl.film_id = f.film_id
            WHERE f.FILM_ID IN (
            	SELECT fd.FILM_ID FROM FILMS_DIRECTORS fd
            	WHERE fd.DIRECTOR_ID = :directorId
            )
            GROUP BY f.film_id, fr.mpa_id
            ORDER BY count(fl.film_id) DESC;""";

    private static final String SELECT_SORTED_DIRECTOR_FILM_BY_YEAR = """
             SELECT f.film_id as film_id,
             		f.film_name as film_name,
            		f.description as description,
            		f.release_date as "release_date",
            		f.duration as duration,
            		fr.mpa_id as mpa_id,
            		fr.mpa_name as mpa_name,
            		count(fl.film_id) as "rate"
            FROM films f
            INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
            LEFT JOIN film_likes fl ON fl.film_id = f.film_id
            WHERE f.FILM_ID IN (
            	SELECT fd.FILM_ID FROM FILMS_DIRECTORS fd
            	WHERE fd.DIRECTOR_ID = :directorId
            )
            GROUP BY f.film_id, fr.mpa_id
            ORDER BY EXTRACT(YEAR FROM f.release_date) ASC;""";

    private final DirectorRowMapper directorRowMapper;

    public DbFilmStorage(NamedParameterJdbcTemplate namedTemplate, FilmRowMapper mapper, DirectorRowMapper directorRowMapper) {
        super(namedTemplate, mapper);
        this.directorRowMapper = directorRowMapper;
    }

    @Override
    public List<Film> getAll() {
        List<Film> films = getAll(FIND_ALL_QUERY);

        return fillFilmsGenresAndDirectors(films);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        List<Film> films = findMany(FIND_TOP_QUERY, Map.of("count", count));

        return fillFilmsGenresAndDirectors(films);
    }

    @Override
    public List<Film> getSortedFilmsByDirector(Director director, String sortBy) {

        String sqlQuery;
        if (sortBy.equalsIgnoreCase("year")) {
            sqlQuery = SELECT_SORTED_DIRECTOR_FILM_BY_YEAR;
        } else {
            sqlQuery = SELECT_SORTED_DIRECTOR_FILM_BY_RATE;
        }

        List<Film> films = findMany(sqlQuery, Map.of("directorId", director.getId()));

        return fillFilmsGenresAndDirectors(films);
    }

    @Override
    public List<Film> getRecommendations(long userId) {
        List<Film> films = findMany(FIND_RECOMMENDATIONS_QUERY, Map.of("userId", userId));

        films.forEach(film -> {
            List<FilmGenre> genres = findMany(FIND_FILM_GENRES_QUERY,
                    Map.of("filmId", film.getId()), new BeanPropertyRowMapper<>(FilmGenre.class));
            film.setGenres(genres);
        });

        return films;
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        List<Film> films = findMany(FIND_COMMON_FILMS, Map.of("userId", userId, "friendId", friendId));

        films.forEach(film -> {
            List<FilmGenre> genres = findMany(FIND_FILM_GENRES_QUERY,
                    Map.of("filmId", film.getId()), new BeanPropertyRowMapper<>(FilmGenre.class));
            film.setGenres(genres);
        });

        return films;
    }

    @Override
    public List<Film> getPopularFilmsByYear(int count, int year) {
        List<Film> films = findMany(FIND_TOP_YEAR, Map.of("count", count, "year", year));
        return addGenresToFilms(films);
    }

    @Override
    public List<Film> getPopularFilmsByGenre(int count, int genre) {
        List<Film> films = findMany(FIND_TOP_GENRE, Map.of("count", count, "genre_id", genre));
        return addGenresToFilms(films);
    }

    @Override
    public List<Film> getPopularFilmsByYearGenre(int count, int year, int genre) {
        List<Film> films = findMany(FIND_TOP_YEAR_GENRE, Map.of("count", count,
                "year", year,
                "genre_id", genre));
        return addGenresToFilms(films);
    }

    @Override
    public Optional<Film> getById(Long filmId) {
        Optional<Film> film = findOne(FIND_BY_ID_QUERY, Map.of("filmId", filmId));

        film.ifPresent(f -> {
            List<FilmGenre> genres = namedTemplate.query(FIND_FILM_GENRES_QUERY,
                    Map.of("filmId", filmId), new BeanPropertyRowMapper<>(FilmGenre.class));
            f.setGenres(genres);

            List<Director>  directors = namedTemplate.query(FIND_FILM_DIRECTORS_QUERY,
                    Map.of("filmId", filmId), directorRowMapper);
            f.setDirectors(directors);
        });

        return film;
    }

    @Override
    public Film save(Film film) {

        if (film.getId() == null) {

            KeyHolder keyHolder = insert(
                    INSERT_QUERY,
                    Map.of(
                        "name", film.getName(),
                        "description", film.getDescription(),
                        "releaseDate", film.getReleaseDate(),
                        "duration", film.getDuration(),
                        "mpaId", film.getMpa().getId()),
                    new String[]{"film_id"}
            );
            Long id = keyHolder.getKeyAs(Long.class);
            if (id == null) {
                throw new FailedToCreateEntity("не удалось создать фильм " + film);
            } else {
                film.setId(id);
                log.debug("Фильм {} сохранен с id = {}", film.getName(), film.getId());
            }

        } else {

            update(
                    UPDATE_QUERY,
                    Map.of(
                        "name", film.getName(),
                        "description", film.getDescription(),
                        "releaseDate", film.getReleaseDate(),
                        "duration", film.getDuration(),
                        "mpaId", film.getMpa().getId(),
                        "filmId", film.getId())
            );
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film);
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            saveDirectors(film);
        }

        return film;
    }

    @Override
    public void delete(Film film) {
        delete(DELETE_QUERY, Map.of("filmId", film.getId()));
    }

    @Override
    public int deleteAll() {
        return delete(DELETE_ALL_QUERY);
    }

    private List<Film> fillFilmsGenresAndDirectors(List<Film> films) {
        films.forEach(film -> {
            List<FilmGenre> genres = findMany(FIND_FILM_GENRES_QUERY,
                    Map.of("filmId", film.getId()), new BeanPropertyRowMapper<>(FilmGenre.class));
            film.setGenres(genres);
        });

        films.forEach(film -> {
            List<Director> directors = findMany(FIND_FILM_DIRECTORS_QUERY,
                    Map.of("filmId", film.getId()), directorRowMapper);
            film.setDirectors(directors);
        });
        return films;
    }

    private void saveGenres(Film film) {

        delete(DELETE_FILM_GENRE_REL_QUERY, Map.of("filmId", film.getId()));

        List<Map<String, Object>> batchValues = film.getGenres().stream()
                .map(genre -> createFilmGenreMap(film, genre))
                .toList();

        batchUpdate(INSERT_FILM_GENRE_REL_QUERY, batchValues);
    }

    private void saveDirectors(Film film) {
        delete(DELETE_FILM_DIRECTORS_QUERY, Map.of("filmId", film.getId()));

        List<Map<String, Object>> batchValues = film.getDirectors().stream()
                .map(director -> createDirectorMap(film, director))
                .toList();

        batchUpdate(INSERT_FILM_DIRECTORS_QUERY, batchValues);
    }

    private Map<String, Object> createFilmGenreMap(Film film, FilmGenre genre) {
        return Map.of("filmId", film.getId(), "genreId", genre.getId());
    }

    private List<Film> addGenresToFilms(List<Film> films) {
        films.forEach(film -> {
            List<FilmGenre> genres = findMany(FIND_FILM_GENRES_QUERY,
                    Map.of("filmId", film.getId()), new BeanPropertyRowMapper<>(FilmGenre.class));
            film.setGenres(genres);
        });
        return films;
    }

    private Map<String, Object> createDirectorMap(Film film, Director director) {
        return Map.of("filmId", film.getId(), "directorId", director.getId());
    }

    @Override
    public List<Film> searchBy(String queryString, String searchBy) {
        String sqlQuery = switch (searchBy) {
            case "title" -> FIND_FILMS_BY_TITLE_QUERY;
            case "director" -> FIND_FILMS_BY_DIRECTOR_QUERY;
            case "title,director", "director,title" -> FIND_FILMS_BY_FILM_AND_DIRECTOR_QUERY;
            default -> throw new NotFoundException("Неизвестный критерий сортировки",
                    "корректные критерии: title, director и оба");
        };

        List<Film> films = namedTemplate.query(sqlQuery, Map.of("name", "%" + queryString.toLowerCase() + "%"),
                new FilmRowMapper());

        films.forEach(film -> {
            List<FilmGenre> genres = findMany(FIND_FILM_GENRES_QUERY,
                    Map.of("filmId", film.getId()), new BeanPropertyRowMapper<>(FilmGenre.class));
            film.setGenres(genres);
        });
        
        films.forEach(film -> {
            List<FilmGenre> genres = findMany(FIND_FILM_DIRECTORS_QUERY,
                    Map.of("filmId", film.getId()), new BeanPropertyRowMapper<>(FilmGenre.class));
            film.setGenres(genres);
        });

        return films;
    }
}
