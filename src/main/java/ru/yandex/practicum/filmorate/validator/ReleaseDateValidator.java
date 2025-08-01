package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.customannotation.ReleaseDate;

import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<ReleaseDate, LocalDate> {

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true; // Тру поставил потому что у меня есть другая аннотация для проверки null
        }

        LocalDate minDate = LocalDate.of(1895, 12, 28);
        return date.isAfter(minDate) || date.isEqual(minDate);
    }
}
