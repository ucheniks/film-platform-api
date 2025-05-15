package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;


public interface ReviewStorage {
    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReview(long reviewId);

    Optional<Review> getReviewById(long reviewId);

    List<Review> getReviewsByFilmId(long filmId, int count);

    boolean setLikeOrDislike(long reviewId, long userId, boolean isPositive);

    boolean removeLikeOrDislike(long reviewId, long userId);
}
