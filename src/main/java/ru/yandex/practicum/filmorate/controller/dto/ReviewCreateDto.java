package ru.yandex.practicum.filmorate.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewCreateDto {
    @NotNull
    private String content;

    @NotNull
    private Integer filmId;

    @NotNull
    private Integer userId;

    @NotNull
    private Boolean isPositive;

    private int useful;
}
