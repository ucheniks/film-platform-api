package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.dal.MpaRatingRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaRatingService {
    private final MpaRatingRepository mpaRatingRepository;

    public List<MpaRating> getAllMpaRatings() {
        return mpaRatingRepository.findAll();
    }

    public MpaRating getMpaRatingById(Long id) {
        return mpaRatingRepository.getById(id);
    }
}