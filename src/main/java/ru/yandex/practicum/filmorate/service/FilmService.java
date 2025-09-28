package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.dao.FeedEventsDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.FilmsLikesDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final FilmsLikesDbStorage filmsLikesDbStorage;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;
    private final FeedEventsDbStorage feedEventsDbStorage;

    private static final String SORT_BY_YEAR = "year";
    private static final String SORT_BY_LIKES = "likes";

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
        if (film.getMpa() == null) {
            throw new ValidationException("MPA rating is required");
        }

        MpaRating mpa = mpaService.findById(film.getMpa().id());
        film.setMpa(mpa);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> uniqueGenres = new LinkedHashSet<>(film.getGenres());
            film.setGenres(uniqueGenres);
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        Film existingFilm = findById(film.getId());

        if (film.getMpa() != null) {
            MpaRating mpa = mpaService.findById(film.getMpa().id());
            film.setMpa(mpa);
        }

        if (film.getDirectors() == null) {
            film.setDirectors(existingFilm.getDirectors());
        } else if (!film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                directorService.findById(director.getId());
            }
            Set<Director> directors = new LinkedHashSet<>(film.getDirectors());
            film.setDirectors(directors);
        }

        // Проверка и удаление режиссёра при необходимости
        if (!film.getDirectors().isEmpty() && existingFilm.getDirectors() != null && !existingFilm.getDirectors().isEmpty()) {
            // логика удаления режиссёра из film или existingFilm

            film.getDirectors().clear(); // Очистка списка режиссёров
        }

        if (film.getGenres() == null) {
            film.setGenres(existingFilm.getGenres());
        } else if (!film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                genreService.findById(genre.id());
            }
            Set<Genre> uniqueGenres = new LinkedHashSet<>(film.getGenres());
            film.setGenres(uniqueGenres);
        }

        Film updatedFilm = filmStorage.update(film);
        return updatedFilm;
    }

    //
    public void addLike(int filmId, int userId) {
        findById(filmId); // Проверяем существование фильма
        userService.findById(userId); // Проверяем существование пользователя

        try {
            feedEventsDbStorage.save(new FeedEvents(
                    1,
                    System.currentTimeMillis(),
                    userId,
                    "LIKE",
                    "ADD",
                    filmId));
            filmsLikesDbStorage.addLike(filmId, userId);
            log.info("Лайк добавлен. Фильм ID={}, Пользователь ID={}", filmId, userId);
        } catch (DataAccessException e) {
            log.warn("Попытка добавления дублирующего лайка. Фильм ID={}, Пользователь ID={}", filmId, userId);
        }
    }

    //
    public void removeLike(int filmId, int userId) {
        findById(filmId);
        userService.findById(userId);
        feedEventsDbStorage.save(new FeedEvents(
                1,
                System.currentTimeMillis(),
                userId,
                "LIKE",
                "REMOVE",
                filmId));
        filmsLikesDbStorage.removeLike(filmId, userId);
        log.info("Лайк удален. Фильм ID={}, Пользователь ID={}", filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return getPopularFilms(count, null, null);
    }

    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        // Валидация параметров на существование жанра и указание корректного года
        if (genreId != null) {
            genreService.findById(genreId);
        }
        int currentYear = LocalDate.now().getYear();
        if (year != null && (year < 1895 || year > currentYear)) {
            throw new ValidationException("Неккоретный год: " + year);
        }
        return filmStorage.findPopularFilms(count, genreId, year);
    }

    public List<Film> getFilmsByDirector(int id, String sortBy) {
        directorService.findById(id);
        List<Film> films = filmStorage.getAllFilmsFromDirector(id);

        switch (sortBy) {
            case SORT_BY_YEAR:
                return films.stream()
                        .sorted(Comparator.comparing(Film::getReleaseDate))
                        .collect(Collectors.toList());
            case SORT_BY_LIKES:
                return films.stream()
                        .sorted(Comparator
                                .comparingInt((Film f) -> f.getLikes().size()).reversed()
                                .thenComparingInt(Film::getId))
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("неправильный параметр sortBy: " + sortBy);
        }
    }

    public void delete(int id) {
        findById(id);
        filmStorage.delete(id);
        log.info("Фильм с ID={} удален", id);
    }


    public List<Film> getAllFilmsCommon(int userId, int friendId) {
        return filmStorage.getCommon(userId, friendId);
    }

    public List<Film> searchFilms(String query, String by) {
        String searchQuery = query.toLowerCase();
        String[] searchBy = by.split(",");

        boolean searchByTitle = false;
        boolean searchByDirector = false;

        for (String param : searchBy) {
            if ("title".equals(param.trim())) {
                searchByTitle = true;
            } else if ("director".equals(param.trim())) {
                searchByDirector = true;
            }
        }

        if (!searchByTitle && !searchByDirector) {
            throw new ValidationException("Параметр 'by' должен содержать 'title' и/или 'director'");
        }

        return filmStorage.searchFilms(searchQuery, searchByTitle, searchByDirector);
    }
}