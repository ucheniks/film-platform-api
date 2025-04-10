package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User newUser) {
        return userStorage.updateUser(newUser);
    }

    public List<User> addFriend(Long userId, Long friendId) {
        log.info("Добавления друга с id {} пользователю с id {}", friendId, userId);
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        validateFriend(user, friend);
        user.addFriend(friendId);
        friend.addFriend(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        log.info("Друг успешно добавлен: {} \n {}", user, friend);
        return List.of(user, friend);
    }

    public List<User> removeFriend(Long userId, Long friendId) {
        log.info("Удаление друга с id {} у пользователя с id {}", friendId, userId);
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.removeFriend(friendId);
        friend.removeFriend(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        log.info("Друг успешно удалён: {} \n {}", user, friend);
        return List.of(user, friend);
    }

    public List<User> getFriends(Long userId) {
        log.info("Получаем список друзей пользователя с id {}", userId);
        User user = userStorage.getUserById(userId);
        Set<Long> friends = user.getFriends();
        return userStorage.getUsers().stream()
                .filter(u -> friends.contains(u.getId()))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        log.info("Нахождение общих друзей у пользователей с id {} и {}", userId, otherUserId);
        Set<Long> friendsUser = userStorage.getUserById(userId).getFriends();
        Set<Long> friendsOtherUser = userStorage.getUserById((otherUserId)).getFriends();
        friendsUser.retainAll(friendsOtherUser);
        if (friendsUser.isEmpty()) {
            return List.of();
        }
        return userStorage.getUsers().stream()
                .filter(user -> friendsUser.contains(user.getId()))
                .collect(Collectors.toList());
    }

    private void validateFriend(User user, User friend) {
        if (user.getFriends().contains(friend.getId())) {
            log.error("Ошибка при добавлении друга: пользователь с id {} уже находится в друзьях у пользователя с id {}", friend.getId(), user.getId());
            throw new ValidationException("Пользователь с id " + friend.getId() + " уже находится в друзьях у этого пользователя");
        }

        if (friend.getFriends().contains(user.getId())) {
            log.error("Ошибка при добавлении друга: пользователь с id {} уже находится в друзьях у пользователя с id {}", user.getId(), friend.getId());
            throw new ValidationException("Пользователь с id " + user.getId() + " уже находится в друзьях у этого пользователя");
        }
    }

}
