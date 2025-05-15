package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private Long eventId;
    private Long timestamp;
    private Long userId;
    private EventType eventType;
    private EventOperation operation;
    private Long entityId;
}
