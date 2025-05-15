package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.ReviewStorage;

import java.util.List;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage, FilmDbStorage filmDbStorage, UserDbStorage userDbStorage) {
        this.reviewStorage = reviewStorage;
        this.filmDbStorage = filmDbStorage;
        this.userDbStorage = userDbStorage;
    }

    @Transactional
    public Review addReview(Review review) {
        validateUserAndFilmExist(review.getUserId(), review.getFilmId());
        if (review.getContent().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty.");
        }
        return reviewStorage.addReview(review);
    }

    @Transactional
    public Review updateReview(Review review) {
        validateReviewExists(review.getReviewId());
        return reviewStorage.updateReview(review);
    }

    @Transactional
    public void deleteReview(long reviewId) {
        validateReviewExists(reviewId);
        reviewStorage.deleteReview(reviewId);
    }

    public Review getReviewById(long reviewId) {
        return reviewStorage.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review with ID " + reviewId + " not found."));
    }

    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        return reviewStorage.getReviewsByFilmId(filmId, count);
    }

    @Transactional
    public void setLikeOrDislike(long reviewId, long userId, boolean isPositive) {
        validateUserExists(userId);
        validateReviewExists(reviewId);

        boolean success = reviewStorage.setLikeOrDislike(reviewId, userId, isPositive);
        if (!success) {
            throw new IllegalArgumentException("User has already " + (isPositive ? "liked" : "disliked") + " this review.");
        }
    }

    @Transactional
    public void removeLikeOrDislike(long reviewId, long userId) {
        log.debug("Попытка удалить лайк/дизлайк к отзыву {} пользователем {}", reviewId, userId);
        validateUserExists(userId);
        validateReviewExists(reviewId);

        boolean success = reviewStorage.removeLikeOrDislike(reviewId, userId);
        if (!success) {
            throw new IllegalArgumentException("User has not liked or disliked this review.");
        }
    }

    private void validateReviewExists(long reviewId) {
        if (reviewStorage.getReviewById(reviewId).isEmpty()) {
            throw new NotFoundException("Review with ID " + reviewId + " not found.");
        }
    }

    private void validateUserAndFilmExist(long userId, long filmId) {
        validateUserExists(userId);
        validateFilmExists(filmId);
    }

    private void validateUserExists(long userId) {
        if (userDbStorage.getUserById(userId) == null) {
            throw new NotFoundException("User with ID " + userId + " not found.");
        }
    }

    private void validateFilmExists(long filmId) {
        if (filmDbStorage.getFilmById(filmId) == null) {
            throw new NotFoundException("Film with ID " + filmId + " not found.");
        }
    }
}
