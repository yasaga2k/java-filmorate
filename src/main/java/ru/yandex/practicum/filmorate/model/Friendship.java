package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Friendship {
    private final int userId;
    private final int friendId;
    private final boolean confirmed;

}
