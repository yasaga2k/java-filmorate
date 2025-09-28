package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {
    private Long reviewId;
    @NotBlank
    private String content;
    @NotNull
    private Integer filmId;
    @NotNull
    private Integer userId;
    private boolean isPositive;
    private int useful;
}