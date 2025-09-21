package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int idCounter = 1;

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> findById(int id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> findPopularFilms(int count) {
        return findPopularFilms(count, null, null);
    }

    @Override
    public List<Film> findPopularFilms(int count, Integer genreId, Integer year) {
        return films.values().stream()
                .filter(film -> filterByGenre(film, genreId))
                .filter(film -> filterByYear(film, year))
                .sorted(Comparator.comparingInt((Film f) -> -f.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public List<Film> getAllFilmsFromDirector(int directorId) {
        return films.values().stream()
                .filter(f -> f.getDirectors().stream()
                        .anyMatch(d -> d.getId() == directorId))
                .toList();
    }

    @Override
    public List<Film> getCommon(int userId, int friendId) {
        return List.of();
    }

    @Override
    public Film create(Film film) {
        film.setId(idCounter++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void delete(int id) {
        films.remove(id);
    }

    // Методы фильтра
    private boolean filterByGenre(Film film, Integer genreId) {
        if (genreId == null) {
            return true;
        }
        return film.getGenres().stream()
                .anyMatch(genre -> genre.id() == genreId);
    }

    private boolean filterByYear(Film film, Integer year) {
        if (year == null) {
            return true;
        }
        return film.getReleaseDate().getYear() == year;
    }
}