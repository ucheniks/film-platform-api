package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();

    @Override
    public List<User> getUsers() {
        log.info("Получение списка пользователей: ");
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long id) {
        log.info("Получение пользователя по id: {}", id);
        validateUserExists(id);
        return users.get(id);
    }

    @Override
    public User addUser(User user) {
        log.info("Добавление нового пользователя: {}", user);
        validateEmail(user.getEmail());
        user.setFriends(new HashSet<>());
        user.setId(getNextId());
        setDefaultName(user);
        users.put(user.getId(), user);
        emails.add(user.getEmail());
        log.info("Пользователь успешно добавлен: {}", user);
        return user;
    }

    @Override
    public User updateUser(User newUser) {
        log.info("Обновление пользователя: {}", newUser);
        validateUserExists(newUser.getId());
        User oldUser = users.get(newUser.getId());

        if (!oldUser.getEmail().equals(newUser.getEmail())) {
            validateEmail(newUser.getEmail());
            emails.remove(oldUser.getEmail());
            emails.add(newUser.getEmail());
        }

        setDefaultName(newUser);
        if (newUser.getFriends() == null) {
            newUser.setFriends(oldUser.getFriends());
        }
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
            log.error("Ошибка при обновлении/получении пользователя: пользователь с id {} не найден", userId);
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
