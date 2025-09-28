package ru.yandex.practicum.filmorate.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewGetDto {

    private long reviewId;

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
