package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);


    @Override
    public List<Film> getFilms() {
        log.info("Получение списка фильмов: ");
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(Long id) {
        log.info("Получение фильма по id: {}", id);
        validateFilmExists(id);
        return films.get(id);
    }

    @Override
    public Film addFilm(Film film) {
        log.info("Добавление нового фильма: {}", film);
        validateFilmReleaseDate(film);
        film.setId(getNextId());
        film.setLikes(new HashSet<>());
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        log.info("Обновление фильма: {}", newFilm);
        validateFilmExists(newFilm.getId());
        validateFilmReleaseDate(newFilm);
        if (newFilm.getLikes() == null) {
            Set<Long> likes = films.get(newFilm.getId()).getLikes();
            newFilm.setLikes(likes);
        }
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм успешно обновлён: {}", newFilm);
        return newFilm;
    }


    private void validateFilmReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Ошибка при валидации фильма: дата релиза фильма {} раньше {}", film.getName(), MIN_RELEASE_DATE);
            throw new ValidationException("Дата релиза фильма — не раньше " + MIN_RELEASE_DATE);
        }
    }

    private void validateFilmExists(Long filmId) {
        if (!films.containsKey(filmId)) {
            log.error("Ошибка при обновлении фильма: фильм с id {} не найден", filmId);
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }

    private Long getNextId() {
        long maxId = films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++maxId;
    }
}
