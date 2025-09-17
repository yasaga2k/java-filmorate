package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.dao.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final FriendshipDbStorage friendshipDbStorage;
    private final FriendshipStorage friendshipStorage;

    public List<User> findAll() {
        log.info("GET /users - Получение всех пользователей");
        return userStorage.findAll();
    }

    public User findById(int id) {
        return userStorage.findById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", id);
                    return new NotFoundException("Пользователь с id=" + id + " не найден");
                });
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        User createdUser = userStorage.create(user);
        log.info("Пользователь создан: ID={}, Login={}", createdUser.getId(), createdUser.getLogin());
        return createdUser;
    }

    public User update(User user) {
        findById(user.getId()); // Валидация существования
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        User updatedUser = userStorage.update(user);
        log.info("Пользователь обновлен: ID={}", user.getId());
        return updatedUser;
    }

    public void addFriend(int userId, int friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        // Односторонняя дружба - только пользователь добавляет друга
        friendshipDbStorage.add(new Friendship(userId, friendId, true));
        log.info("Пользователь {} добавил пользователя {} в друзья", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        findById(userId);
        findById(friendId);

        friendshipDbStorage.delete(new ru.yandex.practicum.filmorate.model.Friendship(userId, friendId, false));
        log.info("Пользователь {} удалил пользователя {} из друзей", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        findById(userId); // Проверяем существование пользователя

        return friendshipDbStorage.getFriendshipByUserId(userId).stream()
                .map(friendship -> findById(friendship.friendId()))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        findById(userId);
        findById(otherId);

        return friendshipDbStorage.findCommonFriends(userId, otherId).stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public void delete(int id) {
        findById(id);
        userStorage.delete(id);
        log.info("Пользователь с ID={} удален", id);
    }
}