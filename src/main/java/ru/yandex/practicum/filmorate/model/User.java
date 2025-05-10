package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class User {

    private Long id;

    @NotBlank(message = "Электронная почта пользователя не может быть пустой")
    @Email(message = "Электронная почта пользователя неподходящего формата")
    private String email;

    @NotBlank(message = "Логин пользователя не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин пользователя не может содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private Map<Long, FriendshipStatus> friends = new HashMap<>();


    public enum FriendshipStatus {
        UNCONFIRMED,
        CONFIRMED
    }
}