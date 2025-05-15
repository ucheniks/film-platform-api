package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private static final String INSERT_EVENT_QUERY =
            "INSERT INTO events (timestamp, user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";
    private static final String GET_EVENTS_QUERY =
            "SELECT * FROM events WHERE user_id = ? ORDER BY timestamp";
    private final JdbcTemplate jdbc;


    public void addEvent(long userId, EventType eventType, EventOperation operation, long entityId) {
        jdbc.update(INSERT_EVENT_QUERY,
                System.currentTimeMillis(),
                userId,
                eventType.name(),
                operation.name(),
                entityId);
    }

    public List<Event> getUserFeed(long userId) {
        return jdbc.query(GET_EVENTS_QUERY, (rs, rowNum) -> new Event(
                rs.getLong("event_id"),
                rs.getLong("timestamp"),
                rs.getLong("user_id"),
                rs.getString("event_type"),
                rs.getString("operation"),
                rs.getLong("entity_id")
        ), userId);
    }
}
