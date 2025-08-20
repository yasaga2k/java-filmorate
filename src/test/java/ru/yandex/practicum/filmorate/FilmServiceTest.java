package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    private FilmService filmService;
    private UserService userService; // Один экземпляр для всех
    private Film testFilm;
    private User testUser;

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);

        filmService = new FilmService(new InMemoryFilmStorage(), userService);

        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);

        testUser = new User();
        testUser.setEmail("test@mail.com");
        testUser.setLogin("testuser");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
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
    }

    @Test
    void findById_WithExistingId_ShouldReturnFilm() {
        Film createdFilm = filmService.create(testFilm);
        Film foundFilm = filmService.findById(createdFilm.getId());
        assertEquals(createdFilm.getId(), foundFilm.getId());
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowException() {
        assertThrows(NotFoundException.class, () -> filmService.findById(999));
    }

    @Test
    void addLike_WithExistingUser_ShouldAddLike() {
        Film createdFilm = filmService.create(testFilm);

        User createdUser = userService.create(testUser);

        filmService.addLike(createdFilm.getId(), createdUser.getId());

        Film film = filmService.findById(createdFilm.getId());
        assertTrue(film.getLikes().contains(createdUser.getId()));
        assertEquals(1, film.getLikes().size());
    }

    @Test
    void addLike_WithNonExistingFilm_ShouldThrowException() {
        User createdUser = userService.create(testUser);
        assertThrows(NotFoundException.class, () -> filmService.addLike(999, createdUser.getId()));
    }

    @Test
    void addLike_WithNonExistingUser_ShouldThrowException() {
        Film createdFilm = filmService.create(testFilm);
        assertThrows(NotFoundException.class, () -> filmService.addLike(createdFilm.getId(), 999));
    }

    @Test
    void removeLike_ShouldRemoveLike() {
        Film createdFilm = filmService.create(testFilm);
        User createdUser = userService.create(testUser);

        filmService.addLike(createdFilm.getId(), createdUser.getId());
        filmService.removeLike(createdFilm.getId(), createdUser.getId());

        Film film = filmService.findById(createdFilm.getId());
        assertFalse(film.getLikes().contains(createdUser.getId()));
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

        User user1 = userService.create(testUser);

        User user2 = new User();
        user2.setEmail("user2@mail.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        userService.create(user2);

        filmService.addLike(film2.getId(), user1.getId());
        filmService.addLike(film2.getId(), user2.getId());

        List<Film> popularFilms = filmService.getPopularFilms(2);
        assertEquals(2, popularFilms.size());
        assertEquals(film2.getId(), popularFilms.get(0).getId()); // Самый популярный первый
    }
}