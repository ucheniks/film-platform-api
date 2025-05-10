package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class MpaRatingRepository extends BaseDbStorage<MpaRating> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM ratings ORDER BY rating_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM ratings WHERE rating_id = ?";

    public MpaRatingRepository(JdbcTemplate jdbc, RowMapper<MpaRating> mapper) {
        super(jdbc, mapper);
    }

    public List<MpaRating> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<MpaRating> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public MpaRating getById(Long id) {
        log.info("Получение рейтинга");
        return findById(id).orElseThrow(() ->
                new NotFoundException("MPA rating с id " + id + " не найден"));
    }
}