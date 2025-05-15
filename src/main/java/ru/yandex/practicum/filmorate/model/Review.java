package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Long reviewId;
    private String content;

    @NotNull(message = "Тип отзыва cannot be null")
    @JsonProperty("isPositive")
    private Boolean isPositive;

    private Long userId;
    private Long filmId;
    private int useful = 0;
}
