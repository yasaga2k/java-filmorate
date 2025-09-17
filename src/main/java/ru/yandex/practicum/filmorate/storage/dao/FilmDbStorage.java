package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String FIND_ALL_SQL = "SELECT f.*, m.name as mpa_name FROM films f " +
            "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id";
    private static final String FIND_BY_ID_SQL = "SELECT f.*, m.name as mpa_name FROM films f " +
            "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?";
    private static final String FIND_POPULAR_FILMS_SQL = """
                SELECT f.*, m.name as mpa_name, COUNT(l.user_id) as likes_count
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
                LEFT JOIN films_likes l ON f.id = l.film_id
                GROUP BY f.id
                ORDER BY likes_count DESC
                LIMIT ?
            """;

    private static final String FIND_POPULAR_FILMS_BY_GENRE_SQL = """
                SELECT f.*, m.name as mpa_name, COUNT(l.user_id) as likes_count
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
                LEFT JOIN films_likes l ON f.id = l.film_id
                WHERE EXISTS (SELECT 1 FROM film_genres fg WHERE fg.film_id = f.id AND fg.genre_id = ?)
                GROUP BY f.id
                ORDER BY likes_count DESC
                LIMIT ?
            """;
    private static final String GET_ALL_FILMS_ID_WITH_DIRECTOR = "SELECT f.id FROM films f " +
            "LEFT JOIN directors_of_films df ON f.id = df.film_id " +
            "WHERE  df.director_id = ?;";

    private static final String FIND_POPULAR_FILMS_BY_YEAR_SQL = """
                SELECT f.*, m.name as mpa_name, COUNT(l.user_id) as likes_count
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
                LEFT JOIN films_likes l ON f.id = l.film_id
                WHERE YEAR(f.release_date) = ?
                GROUP BY f.id
                ORDER BY likes_count DESC
                LIMIT ?
            """;

    private static final String FIND_POPULAR_FILMS_BY_GENRE_AND_YEAR_SQL = """
                SELECT f.*, m.name as mpa_name, COUNT(l.user_id) as likes_count
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
                LEFT JOIN films_likes l ON f.id = l.film_id
                WHERE EXISTS (SELECT 1 FROM film_genres fg WHERE fg.film_id = f.id AND fg.genre_id = ?)
                AND YEAR(f.release_date) = ?
                GROUP BY f.id
                ORDER BY likes_count DESC
                LIMIT ?
            """;
    private static final String CREATE_SQL = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE films SET name = ?, description = ?, " +
            "release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM films WHERE id = ?";
    private static final String DELETE_GENRES_SQL = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String DELETE_DIRECTOR_SQL = "DELETE FROM directors_of_films WHERE film_id = ?";
    private static final String INSERT_GENRES_SQL = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String INSERT_DIRECTORS_SQL = "INSERT INTO directors_of_films  (film_id, director_id) VALUES (?, ?)";
    private static final String LOAD_GENRES_FOR_FILM_SQL = """
            SELECT DISTINCT g.id, g.name\s
            FROM genres g\s
            JOIN film_genres fg ON g.id = fg.genre_id\s
            WHERE fg.film_id = ?\s
            ORDER BY g.id ASC""";
    private static final String LOAD_DIRECTORS_FOR_FILM_SQL = """
            SELECT DISTINCT g.id, g.name\s
            FROM directors g\s
            JOIN directors_of_films fg ON g.id = fg.director_id\s
            WHERE fg.film_id = ?\s
            ORDER BY g.id ASC""";
    private static final String LOAD_GENRES_FOR_FILMS_SQL = """
             SELECT DISTINCT fg.film_id, g.id, g.name\s
             FROM film_genres fg\s
             JOIN genres g ON fg.genre_id = g.id\s
             WHERE fg.film_id IN (%s)\s
             ORDER BY g.id ASC
            \s""";
    private static final String LOAD_DIRECTORS_FOR_FILMS_SQL = """
             SELECT DISTINCT df.film_id, d.id, d.name\s
             FROM directors_of_films df\s
             JOIN directors d ON df.director_id = d.id\s
             WHERE df.film_id IN (%s)\s
             ORDER BY d.id ASC
            \s""";
    private static final String LOAD_LIKES_FOR_FILM_SQL = "SELECT user_id FROM films_likes WHERE film_id = ?";
    private static final String LOAD_LIKES_FOR_FILMS_SQL = "SELECT film_id, user_id FROM films_likes " +
            "WHERE film_id IN (";
    private static final String SEARCH_BY_TITLE_SQL = """
            SELECT DISTINCT f.*, m.name as mpa_name, COUNT(fl.user_id) as likes_count
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN films_likes fl ON f.id = fl.film_id
            WHERE LOWER(f.name) LIKE ?
            GROUP BY f.id
            ORDER BY likes_count DESC
            """;

    private static final String SEARCH_BY_DIRECTOR_SQL = """
            SELECT DISTINCT f.*, m.name as mpa_name, COUNT(fl.user_id) as likes_count
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN films_likes fl ON f.id = fl.film_id
            LEFT JOIN directors_of_films df ON f.id = df.film_id
            LEFT JOIN directors d ON df.director_id = d.id
            WHERE LOWER(d.name) LIKE ?
            GROUP BY f.id
            ORDER BY likes_count DESC
            """;

    private static final String SEARCH_BY_BOTH_SQL = """
            SELECT DISTINCT f.*, m.name as mpa_name, COUNT(fl.user_id) as likes_count
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN films_likes fl ON f.id = fl.film_id
            LEFT JOIN directors_of_films df ON f.id = df.film_id
            LEFT JOIN directors d ON df.director_id = d.id
            WHERE (LOWER(f.name) LIKE ? OR LOWER(d.name) LIKE ?)
            GROUP BY f.id
            ORDER BY likes_count DESC
            """;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> findAll() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_SQL, this::mapRowToFilm);
        loadGenresForFilms(films);
        loadLikesForFilms(films);
        return films;
    }

    @Override
    public Optional<Film> findById(int id) {
        try {
            Film film = jdbcTemplate.queryForObject(FIND_BY_ID_SQL, this::mapRowToFilm, id);
            if (film != null) {
                loadGenresForFilm(film);
                loadLikesForFilm(film);
                loadDirectorsForFilm(film);
                return Optional.of(film);
            }
            return Optional.empty();
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> findPopularFilms(int count) {
        return findPopularFilms(count, null, null);
    }

    @Override
    public List<Film> findPopularFilms(int count, Integer genreId, Integer year) {
        String sql;
        Object[] params;

        if (genreId != null && year != null) {
            sql = FIND_POPULAR_FILMS_BY_GENRE_AND_YEAR_SQL;
            params = new Object[]{genreId, year, count};
        } else if (genreId != null) {
            sql = FIND_POPULAR_FILMS_BY_GENRE_SQL;
            params = new Object[]{genreId, count};
        } else if (year != null) {
            sql = FIND_POPULAR_FILMS_BY_YEAR_SQL;
            params = new Object[]{year, count};
        } else {
            sql = FIND_POPULAR_FILMS_SQL;
            params = new Object[]{count};
        }

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, params);
        loadGenresForFilms(films);
        loadLikesForFilms(films);
        return films;
    }

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(CREATE_SQL, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().id());
            return stmt;
        }, keyHolder);

        film.setId(keyHolder.getKey().intValue());
        saveGenres(film);
        saveDirectors(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        jdbcTemplate.update(UPDATE_SQL,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().id(),
                film.getId());

        jdbcTemplate.update(DELETE_GENRES_SQL, film.getId());
        saveGenres(film);
        saveDirectors(film);
        return film;
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        String mpaName = rs.getString("mpa_name");
        int mpaId = rs.getInt("mpa_id");
        if (mpaName != null) {
            film.setMpa(new MpaRating(mpaId, mpaName));
        }
        return film;
    }

    @Override
    public List<Film> getAllFilmsFromDirector(int directorId) {
        List<Integer> filmIds = jdbcTemplate.queryForList(GET_ALL_FILMS_ID_WITH_DIRECTOR, Integer.class, directorId);
        List<Film> films = filmIds.stream()
                .map(id -> findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return films;
    }

    @Override
    public List<Film> searchFilms(String query, boolean searchByTitle, boolean searchByDirector) {
        String searchPattern = "%" + query.toLowerCase() + "%";

        List<Film> films;

        if (searchByTitle && searchByDirector) {
            films = jdbcTemplate.query(SEARCH_BY_BOTH_SQL, this::mapRowToFilm, searchPattern, searchPattern);
        } else if (searchByTitle) {
            films = jdbcTemplate.query(SEARCH_BY_TITLE_SQL, this::mapRowToFilm, searchPattern);
        } else if (searchByDirector) {
            films = jdbcTemplate.query(SEARCH_BY_DIRECTOR_SQL, this::mapRowToFilm, searchPattern);
        } else { // Если ни один параметр не задан, возвращаем пустой список
            return List.of();
        }

        if (!films.isEmpty()) {
            loadGenresForFilms(films);
            loadLikesForFilms(films);
            loadDirectorsForFilms(films);
        }
        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            jdbcTemplate.update(DELETE_GENRES_SQL, film.getId());

            // УБИРАЕМ ДУБЛИКАТЫ и СОРТИРУЕМ по ID
            List<Genre> sortedGenres = film.getGenres().stream()
                    .distinct()                          // убираем дубли
                    .sorted(Comparator.comparingInt(Genre::id))  // сортируем по id
                    .toList();

            // Сохраняем отсортированные жанры
            for (Genre genre : sortedGenres) {
                jdbcTemplate.update(INSERT_GENRES_SQL, film.getId(), genre.id());
            }

            // Обновляем жанры фильма отсортированным списком
            film.setGenres(new LinkedHashSet<>(sortedGenres));
        }
    }

    private void saveDirectors(Film film) {
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            jdbcTemplate.update(DELETE_DIRECTOR_SQL, film.getId());

            List<Director> sortedDirectors = film.getDirectors().stream()
                    .distinct()
                    .sorted(Comparator.comparingInt(Director::getId))
                    .toList();

            for (Director director : sortedDirectors) {
                jdbcTemplate.update(INSERT_DIRECTORS_SQL, film.getId(), director.getId());
            }

            film.setDirectors(new LinkedHashSet<>(sortedDirectors));
        }
    }

    private void loadGenresForFilm(Film film) {
        List<Genre> genres = jdbcTemplate.query(LOAD_GENRES_FOR_FILM_SQL,
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")),
                film.getId());

        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void loadDirectorsForFilm(Film film) {
        List<Director> directors = jdbcTemplate.query(LOAD_DIRECTORS_FOR_FILM_SQL,
                (rs, rowNum) -> new Director(rs.getInt("id"), rs.getString("name")),
                film.getId());
        film.setDirectors(new LinkedHashSet<>(directors));
    }

    private void loadDirectorsForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        String filmIds = films.stream()
                .map(f -> String.valueOf(f.getId()))
                .collect(Collectors.joining(","));

        String sql = String.format(LOAD_DIRECTORS_FOR_FILMS_SQL, filmIds);

        jdbcTemplate.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            Director director = new Director(rs.getInt("id"), rs.getString("name"));

            for (Film film : films) {
                if (film.getId() == filmId) {
                    film.getDirectors().add(director);
                    break;
                }
            }
        });
    }

    private void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        String filmIds = films.stream()
                .map(f -> String.valueOf(f.getId()))
                .collect(Collectors.joining(","));

        String sql = String.format(LOAD_GENRES_FOR_FILMS_SQL, filmIds);

        jdbcTemplate.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));

            for (Film film : films) {
                if (film.getId() == filmId) {
                    film.getGenres().add(genre);
                    break;
                }
            }
        });
    }

    private void loadLikesForFilm(Film film) {
        Set<Integer> likes = new HashSet<>(jdbcTemplate.query(LOAD_LIKES_FOR_FILM_SQL,
                (rs, rowNum) -> rs.getInt("user_id"),
                film.getId()));
        film.setLikes(likes);
    }

    private void loadLikesForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        String filmIds = films.stream()
                .map(f -> String.valueOf(f.getId()))
                .collect(Collectors.joining(","));

        String sql = LOAD_LIKES_FOR_FILMS_SQL + filmIds + ")";

        Map<Integer, Set<Integer>> filmLikesMap = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            int userId = rs.getInt("user_id");
            filmLikesMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        });

        films.forEach(film -> film.setLikes(filmLikesMap.getOrDefault(film.getId(), new HashSet<>())));
    }
}