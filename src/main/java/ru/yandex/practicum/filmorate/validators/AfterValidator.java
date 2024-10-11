package ru.yandex.practicum.filmorate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
public class AfterValidator implements ConstraintValidator<After, LocalDate> {

    private LocalDate after;

    @Override
    public void initialize(final After constraintAnnotation) {
        after = LocalDate.parse(constraintAnnotation.after());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return value != null && !value.isBefore(after);
    }
}
