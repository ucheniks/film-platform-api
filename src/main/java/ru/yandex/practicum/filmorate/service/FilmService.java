package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ParameterNotValidException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film newFilm) {
        return filmStorage.updateFilm(newFilm);
    }

    public Film addLike(Long filmId, Long userId) {
        log.info("Добавление лайка фильму с id {} от пользователя с id {}", filmId, userId);
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        validateUser(film, user);
        film.addLike(userId);
        filmStorage.updateFilm(film);
        log.info("Лайк успешно поставлен: {}", film);
        return film;
    }

    public Film removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка у фильма c id {} от пользователя с id {}", filmId, userId);
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        validateUserExists(film, user);
        film.removeLike(userId);
        filmStorage.updateFilm(film);
        log.info("Лайк успешно удалён: {}", film);
        return film;
    }

    public List<Film> getPrioritizedFilms(int count) {
        log.info("Получение списка первых {} по количеству лайков", count);
        validateCount(count);
        List<Film> sortedFilms = filmStorage.getFilms().stream()
                .sorted((film1, film2) -> Integer.compare(film2.getLikes().size(), film1.getLikes().size()))
                .toList();
        int endIndex = Math.min(count, sortedFilms.size());
        return sortedFilms.subList(0, endIndex);
    }


    private void validateUser(Film film, User user) {
        if (film.getLikes().contains(user.getId())) {
            log.error("Ошибка при добавлении лайка: пользователь с id {} уже ставил лайк фильму с id {}", user.getId(), film.getId());
            throw new ValidationException("Пользователь с id " + user.getId() + " уже ставил лайк этому фильму");
        }
    }

    private void validateUserExists(Film film, User user) {
        if (!film.getLikes().contains(user.getId())) {
            log.error("Ошибка при удалении лайка: пользователь с id {} не ставил лайк фильму с id {}", user.getId(), film.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден в лайках у фильма");
        }
    }

    private void validateCount(int count) {
        if (count < 1) {
            throw new ParameterNotValidException("Количество не может быть меньше единицы");
        }
    }
}
