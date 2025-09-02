package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

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
        User createdUser = userStorage.create(user);
        log.info("Пользователь создан: ID={}, Login={}", createdUser.getId(), createdUser.getLogin());
        return createdUser;
    }

    public User update(User user) {
        findById(user.getId()); // Валидация существования
        User updatedUser = userStorage.update(user);
        log.info("Пользователь обновлен: ID={}", user.getId());
        return updatedUser;
    }

    public void addFriend(int userId, int friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователи {} и {} теперь друзья. Друзей у {}: {}, у {}: {}",
                userId, friendId, userId, user.getFriends().size(), friendId, friend.getFriends().size());
    }

    public void removeFriend(int userId, int friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        User user = findById(userId);
        List<User> friends = user.getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
        log.info("Найдено {} друзей пользователя {}", friends.size(), userId);
        return friends;
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = findById(userId);
        User otherUser = findById(otherId);

        // Исправлено: оптимизирован стрим
        List<User> commonFriends = user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(this::findById)
                .collect(Collectors.toList());

        log.info("Найдено {} общих друзей между {} и {}", commonFriends.size(), userId, otherId);
        return commonFriends;
    }
}