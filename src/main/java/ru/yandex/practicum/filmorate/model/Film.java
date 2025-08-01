package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.customannotation.ReleaseDate;

import java.time.LocalDate;

@Data
public class Film {
    private int id;

    @NotBlank(message = "название не может быть пустым")
    private String name;

    @Size(max = 200, message = "максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не должна быть null")
    @ReleaseDate
    private LocalDate releaseDate;

    @NotNull(message = "продолжительность фильма должна быть указана")
    @Positive(message = "продолжительность фильма должна быть положительным числом")
    private int duration;

}
