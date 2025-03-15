package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    @Test
    void addUserWithExistingEmail() {
        UserController controller = new UserController();
        User user1 = new User();
        user1.setEmail("test@mail.ru");
        user1.setLogin("login1");
        user1.setBirthday(LocalDate.of(2005, 6, 19));
        controller.addUser(user1);

        User user2 = new User();
        user2.setEmail("test@mail.ru");
        user2.setLogin("login2");
        user2.setBirthday(LocalDate.of(2005, 6, 19));

        assertThrows(ValidationException.class, () -> controller.addUser(user2));
    }

    @Test
    void updateNonExistentUser() {
        UserController controller = new UserController();
        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2005, 6, 19));

        assertThrows(NotFoundException.class, () -> controller.update(user));
    }
}