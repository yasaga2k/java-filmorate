package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Review {
    private Long reviewId;
    @NotBlank(message = "Содержание отзыва не может быть пустым")
    private String content;
    private boolean isPositive;
    private long userId;
    private int filmId;
    private int useful; // рейтинг полезности
}