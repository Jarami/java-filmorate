package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class FilmTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void givenValidFilm_whenSave_gotSuccess() {

        Film film = new Film(
                null,
                "nisi eiusmod",
                "adipisicing",
                LocalDate.parse("1967-03-25"),
                100);

        assertValid(film);
    }

    @Test
    void givenFilmWithEmptyName_whenSave_gotFail() {

        Film film = new Film(
                null,
                "",
                "adipisicing",
                LocalDate.parse("1967-03-25"),
                100);

        assertInValid(film, List.of("name"));
    }

    @Test
    void givenFilmWithoutName_whenSave_gotFail() {

        Film film = new Film(
                null,
                null,
                "adipisicing",
                LocalDate.parse("1967-03-25"),
                100);

        assertInValid(film, List.of("name"));
    }

    @Test
    void givenFilmWithTooLongDesc_whenSave_gotFail() {

        Film film = new Film(
                null,
                null,
                "Пятеро друзей ( комик-группа «Шарло»), приезжают в город Бризуль. Здесь они хотят разыскать господина Огюста Куглова, который задолжал им деньги, а именно 20 миллионов. о Куглов, который за время «своего отсутствия», стал кандидатом Коломбани.",
                LocalDate.parse("1967-03-25"),
                100);

        assertInValid(film, List.of("description"));
    }

    @Test
    void givenFilmWithNegDuration_whenSave_gotSuccess() {

        Film film = new Film(
                null,
                "nisi eiusmod",
                "adipisicing",
                LocalDate.parse("1967-03-25"),
                -100);

        assertInValid(film, List.of("duration"));
    }

    private void assertValid(Film film) {
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    private void assertInValid(Film film, List<String> expectedFailedProperties) {
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        Set<String> failedProperties = violations.stream().map(v ->
                v.getPropertyPath().toString()).collect(Collectors.toSet());

        for (String expFailedProp : expectedFailedProperties) {
            assertTrue(failedProperties.contains(expFailedProp), "failed properties must contain "
                    + expFailedProp);
        }
    }
}
