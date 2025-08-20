package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    private FilmService filmService;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        filmService = new FilmService(new InMemoryFilmStorage());

        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
    }

    @Test
    void createFilm_ShouldReturnFilmWithId() {
        Film createdFilm = filmService.create(testFilm);

        assertNotNull(createdFilm.getId());
        assertEquals("Test Film", createdFilm.getName());
    }

    @Test
    void findAll_ShouldReturnCreatedFilms() {
        filmService.create(testFilm);
        List<Film> films = filmService.findAll();

        assertEquals(1, films.size());
        assertEquals("Test Film", films.get(0).getName());
    }

    @Test
    void findById_WithExistingId_ShouldReturnFilm() {
        Film createdFilm = filmService.create(testFilm);
        Film foundFilm = filmService.findById(createdFilm.getId());

        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals("Test Film", foundFilm.getName());
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowException() {
        assertThrows(NotFoundException.class, () -> filmService.findById(999));
    }

    @Test
    void updateFilm_ShouldUpdateFilmData() {
        Film createdFilm = filmService.create(testFilm);

        Film updatedFilm = new Film();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated Description");
        updatedFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        updatedFilm.setDuration(150);

        Film result = filmService.update(updatedFilm);

        assertEquals("Updated Film", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(150, result.getDuration());
    }

    @Test
    void addLike_ShouldAddLikeToFilm() {
        Film createdFilm = filmService.create(testFilm);
        filmService.addLike(createdFilm.getId(), 1);

        Film film = filmService.findById(createdFilm.getId());
        assertTrue(film.getLikes().contains(1));
        assertEquals(1, film.getLikes().size());
    }

    @Test
    void removeLike_ShouldRemoveLikeFromFilm() {
        Film createdFilm = filmService.create(testFilm);
        filmService.addLike(createdFilm.getId(), 1);
        filmService.removeLike(createdFilm.getId(), 1);

        Film film = filmService.findById(createdFilm.getId());
        assertFalse(film.getLikes().contains(1));
        assertEquals(0, film.getLikes().size());
    }

    @Test
    void getPopularFilms_ShouldReturnFilmsSortedByLikes() {
        Film film1 = filmService.create(testFilm);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(100);
        filmService.create(film2);

        // Добавляем лайки второму фильму
        filmService.addLike(film2.getId(), 1);
        filmService.addLike(film2.getId(), 2);

        List<Film> popularFilms = filmService.getPopularFilms(2);

        assertEquals(2, popularFilms.size());
        assertEquals(film2.getId(), popularFilms.get(0).getId()); // Самый популярный первый
        assertEquals(2, popularFilms.get(0).getLikes().size());
    }
}