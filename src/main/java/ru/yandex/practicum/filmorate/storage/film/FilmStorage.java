package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> findAll();

    Optional<Film> findById(int id);

    Film create(Film film);

    Film update(Film film);

    void delete(int id);

    // Новый метод с фильтрацией
    List<Film> findPopularFilms(int count, Integer genreId, Integer year);

    // Старый метод для обратной совместимости
    List<Film> findPopularFilms(int count);

    List<Film> getAllFilmsFromDirector(int directorId);

    List<Film> searchFilms(String query, boolean searchByTitle, boolean searchByDirector);
}
