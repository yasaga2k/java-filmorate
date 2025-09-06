package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class})
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;

    @Test
    void testFindFilmById() {
        // Given
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);

        // When
        Optional<Film> filmOptional = filmStorage.findById(createdFilm.getId());

        // Then
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("id", createdFilm.getId())
                );
    }

    @Test
    void testFindAllFilms() {
        // Given
        Film film1 = createTestFilm();
        Film film2 = createTestFilm();
        film2.setName("Another Film");
        film2.setMpa(new MpaRating(2, "PG")); // Исправлено: объект MPA

        filmStorage.create(film1);
        filmStorage.create(film2);

        // When
        List<Film> films = filmStorage.findAll();

        // Then
        assertThat(films).hasSize(2);
    }

    @Test
    void testCreateFilm() {
        // Given
        Film film = createTestFilm();

        // When
        Film createdFilm = filmStorage.create(film);

        // Then
        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isPositive();
        assertThat(createdFilm.getName()).isEqualTo("Test Film");
        assertThat(createdFilm.getMpa()).isNotNull();
        assertThat(createdFilm.getMpa().id()).isEqualTo(1);
        assertThat(createdFilm.getMpa().name()).isEqualTo("G");
    }

    @Test
    void testUpdateFilm() {
        // Given
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);

        createdFilm.setName("Updated Film");
        createdFilm.setDescription("Updated Description");
        createdFilm.setMpa(new MpaRating(3, "PG-13")); // Исправлено: объект MPA

        // When
        Film updatedFilm = filmStorage.update(createdFilm);

        // Then
        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedFilm.getMpa().id()).isEqualTo(3);
        assertThat(updatedFilm.getMpa().name()).isEqualTo("PG-13");
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new MpaRating(1, "G")); // Исправлено: объект вместо mpaId
        return film;
    }
}