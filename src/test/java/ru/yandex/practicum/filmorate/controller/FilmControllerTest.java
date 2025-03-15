package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    @Test
    void addFilmWithInvalidReleaseDate() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(322);

        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void updateNonExistentFilm() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setId(1L);
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2005, 6, 19));
        film.setDuration(322);

        assertThrows(NotFoundException.class, () -> controller.updateFilm(film));
    }
}