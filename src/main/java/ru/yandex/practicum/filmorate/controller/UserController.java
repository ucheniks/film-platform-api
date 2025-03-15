package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        log.info("Добавление нового пользователя: {}", user);
        if (emails.contains(user.getEmail())) {
            log.error("Ошибка при добавлении пользователя: email {} уже существует", user.getEmail());
            throw new ValidationException("Пользователь с email " + user.getEmail() + " уже существует");
        }
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Пользователь успешно добавлен: {}", user);
        emails.add(user.getEmail());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Обновление пользователя: {}", newUser);
        if (!users.containsKey(newUser.getId())) {
            log.error("Ошибка при обновлении пользователя: пользователь с id {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id " + newUser.getId() + " не найден");
        }
        User oldUser = users.get(newUser.getId());
        if (!oldUser.getEmail().equals(newUser.getEmail())) {
            if (emails.contains(newUser.getEmail())) {
                log.error("Ошибка при обновлении пользователя: email {} уже существует", newUser.getEmail());
                throw new ValidationException("Пользователь с email " + newUser.getEmail() + " уже существует");
            }
        }
        emails.remove(oldUser.getEmail());
        emails.add(newUser.getEmail());

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }

        users.put(newUser.getId(), newUser);
        log.info("Пользователь успешно обновлён: {}", newUser);
        return newUser;
    }


    private Long getNextId() {
        long maxId = users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++maxId;
    }

}

