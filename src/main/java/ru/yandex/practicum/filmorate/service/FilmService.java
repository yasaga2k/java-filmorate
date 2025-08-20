package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(int id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id={} не найден", id);
                    return new NotFoundException("Фильм с id=" + id + " не найден");
                });
    }

    public Film create(Film film) {
        Film createdFilm = filmStorage.create(film);
        log.info("Фильм создан: ID={}, Name={}", createdFilm.getId(), createdFilm.getName());
        return createdFilm;
    }

    public Film update(Film film) {
        findById(film.getId());
        Film updatedFilm = filmStorage.update(film);
        log.info("Фильм обновлен: ID={}", film.getId());
        return updatedFilm;
    }

    public void addLike(int filmId, int userId) {
        Film film = findById(filmId);
        film.getLikes().add(userId);
        log.info("Лайк добавлен. Фильм ID={}, лайков: {}", filmId, film.getLikes().size());
    }

    public void removeLike(int filmId, int userId) {
        Film film = findById(filmId);
        if (!film.getLikes().remove(userId)) {
            throw new NotFoundException("Лайк не найден");
        }
        log.info("Лайк удален. Фильм ID={}, лайков: {}", filmId, film.getLikes().size());
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> popularFilms = filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
        log.info("Найдено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }
}
