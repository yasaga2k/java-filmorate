package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.dao.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbStorage directorDbStorage;
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    public List<Director> findAll() {
        return directorDbStorage.findAll();
    }

    public Director findById(int id) {
        return directorDbStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Директор с id={} не найден", id);
                    return new NotFoundException("Директор с id=" + id + " не найден");
                });
    }

    public Director create(Director director) {
        return directorDbStorage.create(director);
    }

    public Director update(Director director) {
        directorDbStorage.findById(director.getId()).orElseThrow(() -> {
            log.warn("Директор с id={} не найден", director.getId());
            return new NotFoundException("Директор с id=" + director.getId() + " не найден");
        });
        return directorDbStorage.update(director);
    }

    public void deleteById(int id) {
        directorDbStorage.deleteById(id);
    }

}
