package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FeedEvents;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.dao.FeedEventsDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.FilmsLikesDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final FriendshipDbStorage friendshipDbStorage;
    private final FriendshipStorage friendshipStorage;
    private final FilmsLikesDbStorage filmsLikesDbStorage;
    private final FeedEventsDbStorage feedEventsDbStorage;

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

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
        feedEventsDbStorage.save(new FeedEvents(
                1,
                System.currentTimeMillis(),
                userId,
                "FRIEND",
                "ADD",
                friendId));
        feedEventsDbStorage.save(new FeedEvents(
                1,
                System.currentTimeMillis(),
                friendId,
                "FRIEND",
                "ADD",
                userId));
    }

    public void removeFriend(int userId, int friendId) {
        findById(userId);
        findById(friendId);

        friendshipDbStorage.delete(new ru.yandex.practicum.filmorate.model.Friendship(userId, friendId, false));
        log.info("Пользователь {} удалил пользователя {} из друзей", userId, friendId);
        feedEventsDbStorage.save(new FeedEvents(
                1,
                System.currentTimeMillis(),
                userId,
                "FRIEND",
                "REMOVE",
                friendId));
        feedEventsDbStorage.save(new FeedEvents(
                1,
                System.currentTimeMillis(),
                friendId,
                "FRIEND",
                "REMOVE",
                userId));
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

    public List<Film> getRecommendations(int userId) {
        findById(userId); // Проверяем существование пользователя

        // Загружаем все лайки
        List<Map<String, Integer>> allLikes = filmsLikesDbStorage.getAllLikes();

        // Группируем лайки по пользователям
        Map<Integer, Set<Integer>> userLikesMap = new HashMap<>();
        for (Map<String, Integer> like : allLikes) {
            Integer likeUserId = like.get("userId");
            Integer filmId = like.get("filmId");
            userLikesMap.computeIfAbsent(likeUserId, k -> new HashSet<>()).add(filmId);
        }

        // Получаем фильмы, которые лайкнул целевой пользователь
        Set<Integer> userLikes = userLikesMap.getOrDefault(userId, Set.of());

        // Если пользователь ничего не лайкал, возвращаем пустой список
        if (userLikes.isEmpty()) {
            log.info("Пользователь {} не лайкал фильмы, рекомендации невозможны", userId);
            return List.of();
        }

        // Находим пользователя с максимальным количеством пересечений по лайкам
        Map<Integer, Integer> userSimilarity = new HashMap<>();

        for (Map.Entry<Integer, Set<Integer>> entry : userLikesMap.entrySet()) {
            Integer otherUserId = entry.getKey();
            if (!otherUserId.equals(userId)) {
                Set<Integer> otherUserLikes = entry.getValue();

                // Подсчитываем количество общих лайков
                Set<Integer> intersection = new HashSet<>(userLikes);
                intersection.retainAll(otherUserLikes);

                if (!intersection.isEmpty()) {
                    userSimilarity.put(otherUserId, intersection.size());
                }
            }
        }

        // Если не найдено похожих пользователей, возвращаем пустой список
        if (userSimilarity.isEmpty()) {
            log.info("Не найдены пользователи с похожими вкусами для пользователя {}", userId);
            return List.of();
        }

        // Находим пользователя с максимальным количеством пересечений
        Integer mostSimilarUserId = userSimilarity.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (mostSimilarUserId == null) {
            log.info("Не удалось найти наиболее похожего пользователя для пользователя {}", userId);
            return List.of();
        }

        // Получаем фильмы, которые лайкнул похожий пользователь, но не лайкнул целевой
        Set<Integer> similarUserLikes = userLikesMap.getOrDefault(mostSimilarUserId, Set.of());
        Set<Integer> recommendations = new HashSet<>(similarUserLikes);
        recommendations.removeAll(userLikes);

        log.info("Найден похожий пользователь {} с {} общими лайками. Рекомендовано {} фильмов для пользователя {}",
                mostSimilarUserId, userSimilarity.get(mostSimilarUserId), recommendations.size(), userId);

        // Возвращаем рекомендованные фильмы
        return recommendations.stream()
                .map(filmId -> filmStorage.findById(filmId))
                .filter(filmOpt -> filmOpt.isPresent())
                .map(filmOpt -> filmOpt.get())
                .collect(Collectors.toList());
    }

    public void delete(int id) {
        findById(id);
        userStorage.delete(id);
        log.info("Пользователь с ID={} удален", id);
    }

    public List<FeedEvents> getFeedEvents(int id) {
        User user = findById(id);
        List<User> friends = getFriends(id);
        List<FeedEvents> feedEvents = new ArrayList<>();
        for (User fr : friends) {
            feedEvents.addAll(feedEventsDbStorage.findByUserId(fr.getId()));
        }
        feedEvents.addAll(feedEventsDbStorage.findByUserId(id));
        return feedEvents;

    }

}