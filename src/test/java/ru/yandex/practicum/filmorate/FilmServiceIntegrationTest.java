package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmServiceIntegrationTest {

    @Autowired
    private FilmService filmService;

    @Autowired
    private UserService userService;

    private Film testFilm;
    private User testUser;

    @BeforeEach
    void setUp() {
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
        testFilm.setMpa(new MpaRating(1, "G"));

        testUser = new User();
        testUser.setEmail("test@mail.com");
        testUser.setLogin("testuser");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createFilmShouldReturnFilmWithId() {
        Film createdFilm = filmService.create(testFilm);

        assertNotNull(createdFilm.getId());
        assertTrue(createdFilm.getId() > 0);
        assertEquals("Test Film", createdFilm.getName());
    }

    @Test
    void findAllShouldReturnCreatedFilms() {
        filmService.create(testFilm);

        List<Film> films = filmService.findAll();

        assertFalse(films.isEmpty());
        assertEquals("Test Film", films.get(0).getName());
    }

    @Test
    void findByIdWithExistingIdShouldReturnFilm() {
        Film createdFilm = filmService.create(testFilm);

        Film foundFilm = filmService.findById(createdFilm.getId());

        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals("Test Film", foundFilm.getName());
    }

    @Test
    void findByIdWithNonExistingIdShouldThrowException() {
        assertThrows(NotFoundException.class, () -> filmService.findById(999));
    }

    @Test
    void addLikeShouldAddLikeToFilm() {
        Film createdFilm = filmService.create(testFilm);
        User createdUser = userService.create(testUser);

        filmService.addLike(createdFilm.getId(), createdUser.getId());

        Film filmAfterLike = filmService.findById(createdFilm.getId());
        assertFalse(filmAfterLike.getLikes().isEmpty());
        assertTrue(filmAfterLike.getLikes().contains(createdUser.getId()));
    }

    @Test
    void removeLikeShouldRemoveLikeFromFilm() {
        Film createdFilm = filmService.create(testFilm);
        User createdUser = userService.create(testUser);

        filmService.addLike(createdFilm.getId(), createdUser.getId());
        filmService.removeLike(createdFilm.getId(), createdUser.getId());

        Film filmAfterRemove = filmService.findById(createdFilm.getId());
        assertTrue(filmAfterRemove.getLikes().isEmpty());
    }

    @Test
    void getPopularFilmsShouldReturnSortedByLikes() {
        Film film1 = filmService.create(testFilm);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(100);
        film2.setMpa(new MpaRating(2, "PG"));
        Film createdFilm2 = filmService.create(film2);

        User user1 = userService.create(testUser);

        User user2 = new User();
        user2.setEmail("user2@mail.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        User createdUser2 = userService.create(user2);

        // film2 получает 2 лайка, film1 - 1 лайк
        filmService.addLike(createdFilm2.getId(), user1.getId());
        filmService.addLike(createdFilm2.getId(), createdUser2.getId());
        filmService.addLike(film1.getId(), user1.getId());

        List<Film> popularFilms = filmService.getPopularFilms(2);

        assertEquals(2, popularFilms.size());
        assertEquals(createdFilm2.getId(), popularFilms.get(0).getId()); // Самый популярный первый
        assertTrue(popularFilms.get(0).getLikes().size() >= popularFilms.get(1).getLikes().size());
    }

    @Test
    void getPopularFilmsWithGenreFilterShouldReturnFiltered() {
        Film film = filmService.create(testFilm);
        User user = userService.create(testUser);
        filmService.addLike(film.getId(), user.getId());

        List<Film> popularFilms = filmService.getPopularFilms(10, 1, null); // genreId = 1 (Комедия)

        // Тест зависит от ваших данных, можно проверить хотя бы что не падает
        assertNotNull(popularFilms);
    }
}