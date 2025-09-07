package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.dao.FilmsLikesDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FilmServiceTest {
    private FilmService filmService;

    @Mock
    private FilmStorage filmStorage;

    @Mock
    private UserService userService;

    @Mock
    private FilmsLikesDbStorage filmsLikesDbStorage;

    @Mock
    private MpaService mpaService;

    @Mock
    private GenreService genreService;

    private Film testFilm;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filmService = new FilmService(filmStorage, userService, filmsLikesDbStorage, mpaService, genreService);

        testFilm = new Film();
        testFilm.setId(1);
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
        testFilm.setMpa(new MpaRating(1, "G"));

        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@mail.com");
        testUser.setLogin("testuser");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createFilm_ShouldReturnFilmWithId() {
        when(filmStorage.create(any(Film.class))).thenReturn(testFilm);

        Film createdFilm = filmService.create(testFilm);

        assertNotNull(createdFilm.getId());
        assertEquals("Test Film", createdFilm.getName());
        verify(filmStorage).create(testFilm);
    }

    @Test
    void findAll_ShouldReturnCreatedFilms() {
        when(filmStorage.findAll()).thenReturn(List.of(testFilm));

        List<Film> films = filmService.findAll();

        assertEquals(1, films.size());
        verify(filmStorage).findAll();
    }

    @Test
    void findById_WithExistingId_ShouldReturnFilm() {
        when(filmStorage.findById(1)).thenReturn(Optional.of(testFilm));

        Film foundFilm = filmService.findById(1);

        assertEquals(1, foundFilm.getId());
        verify(filmStorage).findById(1);
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowException() {
        when(filmStorage.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.findById(999));
        verify(filmStorage).findById(999);
    }

    @Test
    void addLike_WithExistingUser_ShouldAddLike() {
        when(filmStorage.findById(1)).thenReturn(Optional.of(testFilm));
        when(userService.findById(1)).thenReturn(testUser);

        filmService.addLike(1, 1);

        verify(filmsLikesDbStorage).addLike(1, 1);
        verify(filmStorage).findById(1);
        verify(userService).findById(1);
    }

    @Test
    void addLike_WithNonExistingFilm_ShouldThrowException() {
        when(filmStorage.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.addLike(999, 1));
        verify(filmStorage).findById(999);
        verify(userService, never()).findById(anyInt());
    }

    @Test
    void addLike_WithNonExistingUser_ShouldThrowException() {
        when(filmStorage.findById(1)).thenReturn(Optional.of(testFilm));
        when(userService.findById(999)).thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class, () -> filmService.addLike(1, 999));
        verify(filmStorage).findById(1);
        verify(userService).findById(999);
    }

    @Test
    void removeLike_ShouldRemoveLike() {
        when(filmStorage.findById(1)).thenReturn(Optional.of(testFilm));
        when(userService.findById(1)).thenReturn(testUser);

        filmService.removeLike(1, 1);

        verify(filmsLikesDbStorage).removeLike(1, 1);
        verify(filmStorage).findById(1);
        verify(userService).findById(1);
    }

    @Test
    void getPopularFilms_ShouldReturnFilmsSortedByLikes() {
        Film film2 = new Film();
        film2.setId(2);
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(100);
        film2.setMpa(new MpaRating(2, "PG"));
        film2.getLikes().add(1);
        film2.getLikes().add(2);

        testFilm.getLikes().add(1);

        when(filmStorage.findPopularFilms(2)).thenReturn(List.of(film2, testFilm));

        List<Film> popularFilms = filmService.getPopularFilms(2);

        assertEquals(2, popularFilms.size());
        assertEquals(2, popularFilms.getFirst().getId()); // Самый популярный первый
        assertEquals(2, popularFilms.getFirst().getLikes().size());
        verify(filmStorage).findPopularFilms(2); // Проверяем вызов нового метода
    }
}