package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO likes(film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_POPULAR_QUERY = "SELECT f.* FROM films f LEFT JOIN likes l ON f.film_id = l.film_id GROUP BY f.film_id ORDER BY COUNT(l.user_id) DESC LIMIT ?";
    private static final String GET_LIKED_FILMS = "SELECT film_id FROM likes WHERE user_id = ?";
    private static final String GET_SIMILAR_USER = "SELECT user_id FROM likes WHERE film_id IN (SELECT film_id FROM likes WHERE user_id = ?) AND user_id != ? GROUP BY user_id ORDER BY COUNT(film_id) DESC LIMIT 1";
    private static final String GET_SIMILAR_USER_LIKED_FILMS = "SELECT  f.* FROM films f JOIN likes l ON f.film_id = l.film_id WHERE user_id IN (" + GET_SIMILAR_USER + ")";
    private static final String GET_FILM_RECOMMENDATIONS = GET_SIMILAR_USER_LIKED_FILMS + "AND l.film_id NOT IN (" + GET_LIKED_FILMS + ")";

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);


    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final MpaRatingRepository mpaRatingRepository;
    private final GenreRepository genreRepository;

    public FilmDbStorage(JdbcTemplate jdbc,
                         FilmRowMapper filmRowMapper,
                         MpaRatingRepository mpaRatingRepository,
                         GenreRepository genreRepository) {
        super(jdbc, filmRowMapper);
        this.jdbc = jdbc;
        this.filmRowMapper = filmRowMapper;
        this.mpaRatingRepository = mpaRatingRepository;
        this.genreRepository = genreRepository;
    }

    @Override
    public List<Film> getFilms() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Film getFilmById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    @Override
    public Film addFilm(Film film) {
        validateFilmReleaseDate(film);
        log.info("Добавление фильма с id {} на уровне репозитория 1", film.getId());
        film = setMpaAndGenresToFilm(film);
        log.info("Добавление фильма с id {} на уровне репозитория 2", film.getId());
        long id = insert(INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId());
        film.setId(id);
        addGenresToDb(film);
        log.info("Добавление жанров к фильму с id {} на уровне репозитория", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        validateFilmReleaseDate(film);
        film = setMpaAndGenresToFilm(film);
        update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        addGenresToDb(film);
        return film;
    }

    public void addLike(Long filmId, Long userId) {
        jdbc.update(ADD_LIKE_QUERY, filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        jdbc.update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return jdbc.query(GET_POPULAR_QUERY, filmRowMapper, count);
    }

    public List<Film> getRecommendations(Long userId) {
        return jdbc.query(GET_FILM_RECOMMENDATIONS, mapper, userId, userId, userId);
    }

    private Film setMpaAndGenresToFilm(Film film) {
        Long mpaId = film.getMpa().getId();
        film.setMpa(mpaRatingRepository.getById(mpaId));
        List<Long> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .toList();
        validateGenres(genreIds);
        Set<Genre> genres = genreRepository.findByIds(genreIds);
        film.setGenres(genres);
        return film;
    }

    private void addGenresToDb(Film film) {
        List<Long> genreIds = new ArrayList<>(film.getGenres().stream()
                .map(Genre::getId)
                .toList());
        genreRepository.addGenresToFilm(film.getId(), genreIds);
    }

    private void validateFilmReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Ошибка при валидации фильма: дата релиза фильма {} раньше {}", film.getName(), MIN_RELEASE_DATE);
            throw new ValidationException("Дата релиза фильма — не раньше " + MIN_RELEASE_DATE);
        }
    }

    private void validateGenres(List<Long> genreIds) {
        boolean isValid = genreIds.stream()
                .allMatch(id -> id >= 1 && id <= 6);

        if (!isValid) {
            throw new NotFoundException("Id жанров должны быть в диапазоне от 1 до 6");
        }
    }
}
