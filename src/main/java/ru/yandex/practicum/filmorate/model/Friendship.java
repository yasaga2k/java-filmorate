package ru.yandex.practicum.filmorate.model;

public record Friendship(int userId, int friendId, boolean confirmed) {
}