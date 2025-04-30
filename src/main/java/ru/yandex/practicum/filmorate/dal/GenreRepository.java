package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

@Slf4j
@Repository
public class GenreRepository extends BaseDbStorage<Genre> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres ORDER BY genre_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String FIND_BY_FILM_QUERY = """
            SELECT g.*
            FROM genres g
            JOIN film_genres fg ON g.genre_id = fg.genre_id
            WHERE fg.film_id = ?
            ORDER BY g.genre_id""";
    private static final String INSERT_GENRE_QUERY = "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";

    private final JdbcTemplate jdbc;

    public GenreRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
        this.jdbc = jdbc;
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Genre> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Genre getById(Long id) {
        log.info("Получение рейтинга");
        return findById(id).orElseThrow(() ->
                new NotFoundException("Жанр  с id " + id + " не найден"));
    }

    public List<Genre> findByFilmId(Long filmId) {
        return findMany(FIND_BY_FILM_QUERY, filmId);
    }

    public void addGenreToFilm(Long filmId, Long genreId) {
        log.info("Добавление жанра к фильму");
        jdbc.update(INSERT_GENRE_QUERY, filmId, genreId);
        log.info("Добавил к фильму жанр");
    }

    public void removeAllGenresFromFilm(Long filmId) {
        jdbc.update(DELETE_GENRES_QUERY, filmId);
    }
}