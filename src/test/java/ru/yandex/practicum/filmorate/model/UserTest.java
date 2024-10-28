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
public class UserTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void givenValidUser_whenSave_gotSuccess() {
        User user = new User(
                "mail@mail.ru",
                "dolore",
                "Nick Name",
                LocalDate.parse("1946-08-20"));

        assertValid(user);
    }

    @Test
    void givenUserWithoutName_whenSave_gotSuccess() {
        User user = new User(
                "mail@mail.ru",
                "dolore",
                null,
                LocalDate.parse("1946-08-20"));

        assertValid(user);
    }

    @Test
    void givenNoLogin_whenSave_gotFail() {
        User user = new User(
                "hello@main.ru",
                null,
                "Nick Name",
                LocalDate.parse("1946-08-20"));

        assertInValid(user, List.of("login"));
    }

    @Test
    void givenLoginWithSpace_whenSave_gotFail() {
        User user = new User(
                "hello@main.ru",
                "Chuck Norris",
                "Nick Name",
                LocalDate.parse("1946-08-20"));

        assertInValid(user, List.of("login"));
    }

    @Test
    void givenLoginWithWrongEmail_whenSave_gotFail() {
        User user = new User(
                "@mail.ru",
                "dolore",
                "Nick Name",
                LocalDate.parse("1946-08-20"));

        assertInValid(user, List.of("email"));
    }

    @Test
    void givenLoginWithWrongBirthday_whenSave_gotFail() {
        User user = new User(
                "mail@mail.ru",
                "dolore",
                "Nick Name",
                LocalDate.parse("2946-08-20"));

        assertInValid(user, List.of("birthday"));
    }

    private void assertValid(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    private void assertInValid(User user, List<String> expectedFailedProperties) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        Set<String> failedProperties = violations.stream().map(v ->
                v.getPropertyPath().toString()).collect(Collectors.toSet());

        for (String expFailedProp : expectedFailedProperties) {
            assertTrue(failedProperties.contains(expFailedProp), "failed properties must contain "
                    + expFailedProp);
        }
    }
}
