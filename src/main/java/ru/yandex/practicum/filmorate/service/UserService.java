package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exceptions.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class UserService {
    private final UserDbStorage userStorage;

    public List<User> getUsers() {
        log.info("Получение списка всех пользователей");
        return userStorage.getUsers();
    }

    public User getUserById(Long id) {
        log.info("Получение пользователя с ID: {}", id);
        return userStorage.getUserById(id);
    }

    public User addUser(User user) {
        log.info("Добавление нового пользователя: {}", user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Для пользователя {} установлено имя из логина", user.getEmail());
        }
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        log.info("Обновление пользователя с ID: {}", user.getId());
        return userStorage.updateUser(user);
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Пользователь {} добавляет в друзья пользователя {}", userId, friendId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriends().get(friendId) == User.FriendshipStatus.CONFIRMED) {
            String error = String.format("Пользователь %d уже есть в друзьях у пользователя %d", friendId, userId);
            log.error(error);
            throw new ValidationException(error);
        }
        userStorage.addFriend(userId, friendId);
        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Пользователь {} удаляет из друзей пользователя {}", userId, friendId);
        User user = getUserById(userId);

        /*if (!user.getFriends().containsKey(friendId)) {
            String error = String.format("Пользователь %d не найден в друзьях у пользователя %d", friendId, userId);
            log.error(error);
            throw new NotFoundException(error);
        }*/

        userStorage.removeFriend(userId, friendId);
        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        log.info("Получение списка друзей пользователя {}", userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Поиск общих друзей пользователей {} и {}", userId, otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }
}