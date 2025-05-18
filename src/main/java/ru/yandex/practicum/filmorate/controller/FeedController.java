package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{id}/feed")
@RequiredArgsConstructor
public class FeedController {
    private final EventService eventService;

    @GetMapping
    public List<Event> getUserFeed(@PathVariable long id) {
        log.info("Получение ленты для пользователя с ID {}", id);
        return eventService.getUserFeed(id);
    }
}
