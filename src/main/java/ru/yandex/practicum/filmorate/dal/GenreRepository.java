package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Stream;

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
    private static final String DELETE_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String FIND_BY_IDS_QUERY = "SELECT * FROM genres WHERE genre_id IN (%s)";
    private static final String INSERT_GENRES_QUERY = "INSERT INTO film_genres(film_id, genre_id) VALUES %s";

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

    public Set<Genre> findByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return new HashSet<>();
        }

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String query = String.format(FIND_BY_IDS_QUERY, placeholders);

        Object[] params = ids.toArray();

        return new LinkedHashSet<>(findMany(query, params));
    }

    public void addGenresToFilm(Long filmId, List<Long> ids) {
        if (ids.isEmpty()) {
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "(?, ?)"));
        String query = String.format(INSERT_GENRES_QUERY, placeholders);

        Object[] params = ids.stream()
                .flatMap(genreId -> Stream.of(filmId, genreId))
                .toArray();

        log.info("Добавление жанров {} к фильму {}", ids, filmId);
        jdbc.update(query, params);
        log.info("Жанры успешно добавлены");
    }

    public void removeAllGenresFromFilm(Long filmId) {
        jdbc.update(DELETE_GENRES_QUERY, filmId);
    }
}
