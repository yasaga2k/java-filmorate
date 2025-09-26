package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {
    static final String PATH_TO_DIRECTOR_ID = "/{director-id}";
    private final DirectorService directorService;

    @PostMapping
    public Director createDirector(@Valid @RequestBody Director director) {
        return directorService.create(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        return directorService.update(director);
    }

    @GetMapping
    public List<Director> getAllDirectors() {
        return directorService.findAll();
    }

    @DeleteMapping(PATH_TO_DIRECTOR_ID)
    public void deleteDirector(@PathVariable("director-id") int id) {
        directorService.deleteById(id);
    }

    @GetMapping(PATH_TO_DIRECTOR_ID)
    public Optional<Director> getDirectorById(@PathVariable("director-id") int id) {
        return Optional.ofNullable(directorService.findById(id));
    }
}

