package ru.yandex.practicum.filmorate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserLoginValidator implements ConstraintValidator<UserLogin, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && !containsWhitespace(value);
    }

    private boolean containsWhitespace(String s) {
        for (int i = 0; i < s.length(); ++i) {
            if (Character.isWhitespace(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
