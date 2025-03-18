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
        validateEmail(user.getEmail());
        user.setId(getNextId());
        setDefaultName(user);
        users.put(user.getId(), user);
        emails.add(user.getEmail());
        log.info("Пользователь успешно добавлен: {}", user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Обновление пользователя: {}", newUser);
        validateUserExists(newUser.getId());
        User oldUser = users.get(newUser.getId());

        if (!oldUser.getEmail().equals(newUser.getEmail())) {
            validateEmail(newUser.getEmail());
            emails.remove(oldUser.getEmail());
            emails.add(newUser.getEmail());
        }

        setDefaultName(newUser);
        users.put(newUser.getId(), newUser);
        log.info("Пользователь успешно обновлён: {}", newUser);
        return newUser;
    }

    private void validateEmail(String email) {
        if (emails.contains(email)) {
            log.error("Ошибка при добавлении/обновлении пользователя: email {} уже существует", email);
            throw new ValidationException("Пользователь с email " + email + " уже существует");
        }
    }

    private void validateUserExists(Long userId) {
        if (!users.containsKey(userId)) {
            log.error("Ошибка при обновлении пользователя: пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private void setDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public void clear() {
        users.clear();
        emails.clear();
    }

    private Long getNextId() {
        long maxId = users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++maxId;
    }
}