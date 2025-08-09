package com.moviebookingapp.controller;

import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/moviebooking")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @GetMapping("/all")
    public ResponseEntity<List<Movie>> all() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/movies/search/{moviename}")
    public ResponseEntity<List<Movie>> search(@PathVariable("moviename") String moviename) {
        return ResponseEntity.ok(movieService.searchMovies(moviename));
    }

    @PatchMapping("/{moviename}/theatres/{theatre}/tickets")
    public ResponseEntity<Movie> updateTickets(@PathVariable("moviename") String moviename,
                                               @PathVariable("theatre") String theatre,
                                               @RequestParam("total") int total) {
        return ResponseEntity.ok(movieService.updateTotalTickets(moviename, theatre, total));
    }

    @DeleteMapping("/{moviename}/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable String moviename, @PathVariable Long id) {
        movieService.deleteMovieById(id);
        return ResponseEntity.noContent().build();
    }
} 