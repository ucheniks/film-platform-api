package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.exceptions.InternalServerException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.ReviewStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ReviewDbStorage extends BaseDbStorage<Review> implements ReviewStorage {

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbc, ReviewRowMapper reviewRowMapper) {
        super(jdbc, reviewRowMapper);
    }

    @Override
    public Optional<Review> getReviewById(long reviewId) {
        String query = "SELECT * FROM reviews WHERE review_id = ?";
        return findOne(query, reviewId);
    }

    @Override
    public List<Review> getReviewsByFilmId(long filmId, int count) {
        String query = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return findMany(query, filmId, count);
    }

    @Override
    public Review addReview(Review review) {
        String query = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, 0)";
        long reviewId = insert(query,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId());
        review.setReviewId(reviewId);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String query = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        update(query, review.getContent(), review.getIsPositive(), review.getReviewId());
        return review;
    }

    @Override
    public void deleteReview(long reviewId) {
        String query = "DELETE FROM reviews WHERE review_id = ?";
        update(query, reviewId);
    }

    @Override
    public boolean setLikeOrDislike(long reviewId, long userId, boolean isPositive) {
        try {
            log.debug("Попытка установить {} к отзыву {} пользователем {}",
                    isPositive ? "лайк" : "дизлайк", reviewId, userId);

            String checkQuery = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
            int count = jdbc.queryForObject(checkQuery, Integer.class, reviewId, userId);

            if (count == 0) {
                String insertQuery = "INSERT INTO review_likes (review_id, user_id, is_positive) VALUES (?, ?, ?)";
                jdbc.update(insertQuery, reviewId, userId, isPositive);
                updateUsefulCount(reviewId, isPositive ? 1 : -1);
                log.debug("Добавлен {} для отзыва {} пользователем {}",
                        isPositive ? "лайк" : "дизлайк", reviewId, userId);
            } else {
                // Запись уже есть, обновим
                String updateQuery = "UPDATE review_likes SET is_positive = ? WHERE review_id = ? AND user_id = ?";
                jdbc.update(updateQuery, isPositive, reviewId, userId);
                updateUsefulCount(reviewId, isPositive ? 2 : -2);
                log.debug("Обновлен {} для отзыва {} пользователем {}",
                        isPositive ? "лайк" : "дизлайк", reviewId, userId);
            }

            return true;
        } catch (Exception e) {
            log.error("Ошибка при установке {} для отзыва {} пользователем {}: {}",
                    isPositive ? "лайка" : "дизлайка", reviewId, userId, e.getMessage(), e);
            throw new InternalServerException("Не удалось поставить " +
                    (isPositive ? "лайк" : "дизлайк") + " отзыву. Попробуйте позже.");
        }
    }

    private void updateUsefulCount(long reviewId, int delta) {
        String query = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";
        jdbc.update(query, delta, reviewId);
        log.debug("Рейтинг отзыва {} изменен на {}", reviewId, delta > 0 ? "+" + delta : delta);
    }

    @Override
    public boolean removeLikeOrDislike(long reviewId, long userId) {
        try {
            log.debug("Удаляем лайк/дизлайк к отзыву {} пользователем {}", reviewId, userId);
            String query = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
            int affectedRows = jdbc.update(query, reviewId, userId);

            if (affectedRows > 0) {
                log.debug("Лайк/дизлайк удален. Обновляем рейтинг.");
                updateUsefulCount(reviewId, -getUsefulDelta(reviewId, userId));
            }

            return affectedRows > 0;
        } catch (Exception e) {
            log.error("Ошибка при удалении лайка/дизлайка для отзыва {} пользователем {}: {}",
                    reviewId, userId, e.getMessage(), e);
            throw new InternalServerException("Не удалось удалить лайк/дизлайк отзыву. Попробуйте позже.");
        }
    }

    private int getUsefulDelta(long reviewId, long userId) {
        String query = "SELECT is_positive FROM review_likes WHERE review_id = ? AND user_id = ?";
        Boolean isPositive = jdbc.query(query, rs -> rs.next() ? rs.getBoolean("is_positive") : null, reviewId, userId);
        return (isPositive != null && isPositive) ? -1 : 1;
    }

}
