package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
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
@Slf4j
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_ALL_SQL = "SELECT f.*, m.name as mpa_name FROM films f " +
            "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id";
    private static final String FIND_BY_ID_SQL = "SELECT f.*, m.name as mpa_name FROM films f " +
            "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?";

    // Универсальный запрос для популярных фильмов
    private static final String FIND_POPULAR_FILMS_BASE_SQL = """
            SELECT f.*, m.name as mpa_name, COUNT(l.user_id) as likes_count
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN films_likes l ON f.id = l.film_id
            """;
    private static final String FIND_POPULAR_FILMS_GROUP_BY = " GROUP BY f.id ORDER BY likes_count DESC LIMIT ?";

    private static final String GET_ALL_FILMS_ID_WITH_DIRECTOR = "SELECT f.id FROM films f " +
            "LEFT JOIN directors_of_films df ON f.id = df.film_id " +
            "WHERE df.director_id = ?";

    // Универсальный запрос для загрузки связанных сущностей
    private static final String LOAD_RELATED_ENTITIES_SQL = """
            SELECT junction_table.film_id, entity_table.id, entity_table.name
            FROM %s entity_table
            JOIN %s junction_table ON entity_table.id = junction_table.%s
            WHERE junction_table.film_id = ?
            ORDER BY entity_table.id ASC
            """;

    private static final String CREATE_SQL = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE films SET name = ?, description = ?, " +
            "release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM films WHERE id = ?";
    private static final String DELETE_GENRES_SQL = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String DELETE_DIRECTOR_SQL = "DELETE FROM directors_of_films WHERE film_id = ?";
    private static final String INSERT_GENRES_SQL = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String INSERT_DIRECTORS_SQL = "INSERT INTO directors_of_films (film_id, director_id) VALUES (?, ?)";
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
    private static final String LOAD_LIKES_FOR_FILMS_SQL = "SELECT film_id, user_id FROM films_likes WHERE film_id IN (%s)";
    private static final String SEARCH_FILMS_SQL = """
            SELECT DISTINCT f.*, m.name as mpa_name, COUNT(fl.user_id) as likes_count
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN films_likes fl ON f.id = fl.film_id
            LEFT JOIN directors_of_films df ON f.id = df.film_id
            LEFT JOIN directors d ON df.director_id = d.id
            WHERE (? = 'title' AND LOWER(f.name) LIKE ?)
               OR (? = 'director' AND LOWER(d.name) LIKE ?)
               OR (? = 'both' AND (LOWER(f.name) LIKE ? OR LOWER(d.name) LIKE ?))
            GROUP BY f.id
            ORDER BY likes_count DESC
            """;

    // Вынесенный запрос для общих фильмов
    private static final String GET_COMMON_FILMS_SQL = """
             SELECT DISTINCT f.*, m.name AS mpa_name\s
             FROM films_likes AS l\s
             INNER JOIN films AS f ON l.film_id = f.id\s
             LEFT JOIN mpa_ratings m ON f.mpa_id = m.id\s
             WHERE l.user_id=?
            \s""";

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, (rs, rowNum) -> {
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

            film.setDirectors(new LinkedHashSet<>());
            film.setGenres(new LinkedHashSet<>());
            return film;
        });
    }

    @Override
    public List<Film> findAll() {
        List<Film> films = findMany(FIND_ALL_SQL);
        loadGenresForFilms(films);
        loadLikesForFilms(films);
        loadDirectorsForFilms(films);
        return films;
    }

    @Override
    public Optional<Film> findById(int id) {
        Optional<Film> filmOpt = findOne(FIND_BY_ID_SQL, id);
        filmOpt.ifPresent(film -> {
            loadGenresForFilm(film);
            loadLikesForFilm(film);
            loadDirectorsForFilm(film);
        });
        return filmOpt;
    }

    @Override
    public List<Film> findPopularFilms(int count) {
        return findPopularFilms(count, null, null);
    }

    @Override
    public List<Film> findPopularFilms(int count, Integer genreId, Integer year) {
        StringBuilder sqlBuilder = new StringBuilder(FIND_POPULAR_FILMS_BASE_SQL);
        List<Object> params = new ArrayList<>();

        List<String> conditions = new ArrayList<>();
        if (genreId != null) {
            conditions.add("EXISTS (SELECT 1 FROM film_genres fg WHERE fg.film_id = f.id AND fg.genre_id = ?)");
            params.add(genreId);
        }
        if (year != null) {
            conditions.add("YEAR(f.release_date) = ?");
            params.add(year);
        }

        if (!conditions.isEmpty()) {
            sqlBuilder.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        sqlBuilder.append(FIND_POPULAR_FILMS_GROUP_BY);
        params.add(count);

        List<Film> films = findMany(sqlBuilder.toString(), params.toArray());
        loadGenresForFilms(films);
        loadLikesForFilms(films);
        loadDirectorsForFilms(films);
        return films;
    }

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(CREATE_SQL, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().id());
            return stmt;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        saveGenres(film);
        saveDirectors(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        update(UPDATE_SQL,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().id(),
                film.getId());

        jdbc.update(DELETE_GENRES_SQL, film.getId());
        jdbc.update(DELETE_DIRECTOR_SQL, film.getId());
        saveGenres(film);
        saveDirectors(film);
        return film;
    }

    @Override
    public void delete(int id) {
        update(DELETE_SQL, id);
    }

    @Override
    public List<Film> getAllFilmsFromDirector(int directorId) {
        List<Integer> filmIds = jdbc.queryForList(GET_ALL_FILMS_ID_WITH_DIRECTOR, Integer.class, directorId);
        return filmIds.stream()
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<Film> searchFilms(String query, boolean searchByTitle, boolean searchByDirector) {
        String searchPattern = "%" + query.toLowerCase() + "%";

        String searchType;
        Object[] params;

        if (searchByTitle && searchByDirector) {
            searchType = "both";
            params = new Object[]{searchType, searchPattern, searchType, searchPattern, searchType, searchPattern, searchPattern};
        } else if (searchByTitle) {
            searchType = "title";
            params = new Object[]{searchType, searchPattern, searchType, searchPattern, searchType, searchPattern, searchPattern};
        } else if (searchByDirector) {
            searchType = "director";
            params = new Object[]{searchType, searchPattern, searchType, searchPattern, searchType, searchPattern, searchPattern};
        } else {
            return List.of();
        }

        List<Film> films = findMany(SEARCH_FILMS_SQL, params);

        if (!films.isEmpty()) {
            loadGenresForFilms(films);
            loadLikesForFilms(films);
            loadDirectorsForFilms(films);
        }
        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            jdbc.update(DELETE_GENRES_SQL, film.getId());

            List<Genre> sortedGenres = film.getGenres().stream()
                    .distinct()
                    .sorted(Comparator.comparingInt(Genre::id))
                    .toList();

            for (Genre genre : sortedGenres) {
                jdbc.update(INSERT_GENRES_SQL, film.getId(), genre.id());
            }

            film.setGenres(new LinkedHashSet<>(sortedGenres));
        }
    }

    private void saveDirectors(Film film) {
        jdbc.update(DELETE_DIRECTOR_SQL, film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            film.getDirectors().stream()
                    .distinct()
                    .sorted(Comparator.comparingInt(Director::getId))
                    .forEach(director -> jdbc.update(INSERT_DIRECTORS_SQL, film.getId(), director.getId()));

            film.setDirectors(new LinkedHashSet<>(film.getDirectors()));
        }

        log.info("Directors updated for film {}: {}", film.getId(), film.getDirectors());
    }

    private void loadGenresForFilm(Film film) {
        String sql = String.format(LOAD_RELATED_ENTITIES_SQL, "genres", "film_genres", "genre_id");
        List<Genre> genres = jdbc.query(sql,
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")),
                film.getId());
        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void loadDirectorsForFilm(Film film) {
        String sql = String.format(LOAD_RELATED_ENTITIES_SQL, "directors", "directors_of_films", "director_id");
        List<Director> directors = jdbc.query(sql,
                (rs, rowNum) -> new Director(rs.getInt("id"), rs.getString("name")),
                film.getId());
        film.setDirectors(new LinkedHashSet<>(directors));
    }

    private void loadDirectorsForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        Map<Integer, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, f -> f));

        String filmIds = films.stream()
                .map(f -> String.valueOf(f.getId()))
                .collect(Collectors.joining(","));

        String sql = String.format(LOAD_DIRECTORS_FOR_FILMS_SQL, filmIds);

        jdbc.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            Director director = new Director(rs.getInt("id"), rs.getString("name"));

            Film film = filmMap.get(filmId);
            if (film != null) {
                if (film.getDirectors() == null) {
                    film.setDirectors(new LinkedHashSet<>());
                }
                film.getDirectors().add(director);
            }
        });
    }

    private void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        String filmIds = films.stream()
                .map(f -> String.valueOf(f.getId()))
                .collect(Collectors.joining(","));

        String sql = String.format(LOAD_GENRES_FOR_FILMS_SQL, filmIds);

        jdbc.query(sql, rs -> {
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
        Set<Integer> likes = new HashSet<>(jdbc.query(LOAD_LIKES_FOR_FILM_SQL,
                (rs, rowNum) -> rs.getInt("user_id"),
                film.getId()));
        film.setLikes(likes);
    }

    private void loadLikesForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        String filmIds = films.stream()
                .map(f -> String.valueOf(f.getId()))
                .collect(Collectors.joining(","));

        String sql = String.format(LOAD_LIKES_FOR_FILMS_SQL, filmIds);

        Map<Integer, Set<Integer>> filmLikesMap = new HashMap<>();
        jdbc.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            int userId = rs.getInt("user_id");
            filmLikesMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        });

        films.forEach(film -> film.setLikes(filmLikesMap.getOrDefault(film.getId(), new HashSet<>())));
    }

    //Поиск совместного фильма
    public List<Film> getCommon(int userId, int friendId) {
        List<Film> userFilms = jdbc.query(GET_COMMON_FILMS_SQL, mapper, userId);
        List<Film> friendFilms = jdbc.query(GET_COMMON_FILMS_SQL, mapper, friendId);

        Set<Integer> list2Ids = friendFilms.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        List<Film> commonFilms = userFilms.stream()
                .filter(film -> list2Ids.contains(film.getId()))
                .collect(Collectors.toList());

        loadGenresForFilms(commonFilms);
        return commonFilms;
    }
}