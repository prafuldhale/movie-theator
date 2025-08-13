package com.moviebookingapp.controller;

import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/moviebooking")
@RequiredArgsConstructor
@Slf4j
public class MovieController {
    private final MovieService movieService;

    @GetMapping("/all")
    public ResponseEntity<List<Movie>> getAllMovies() {
        log.info("Request to get all movies");
        
        try {
            List<Movie> movies = movieService.getAllMovies();
            log.info("Retrieved {} movies successfully", movies.size());
            log.debug("Movies retrieved: {}", movies.stream().map(Movie::getMovieName).toList());
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            log.error("Error retrieving all movies: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/movies/search/{moviename}")
    public ResponseEntity<List<Movie>> searchMovies(@PathVariable("moviename") String moviename) {
        log.info("Search request for movies with name: {}", moviename);
        
        try {
            List<Movie> movies = movieService.searchMovies(moviename);
            log.info("Found {} movies matching search term: {}", movies.size(), moviename);
            log.debug("Search results: {}", movies.stream().map(Movie::getMovieName).toList());
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            log.error("Error searching movies with term '{}': {}", moviename, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/movies/add")
    public ResponseEntity<Movie> addMovie(@RequestBody Movie movie) {
        log.info("Request to add new movie: {} at theatre: {}", movie.getMovieName(), movie.getTheatreName());
        log.debug("Movie details - totalTickets: {}, status: {}", movie.getTotalTickets(), movie.getStatus());
        
        try {
            Movie savedMovie = movieService.addMovie(movie);
            log.info("Movie added successfully - id: {}, name: {}, theatre: {}", 
                    savedMovie.getId(), savedMovie.getMovieName(), savedMovie.getTheatreName());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMovie);
        } catch (Exception e) {
            log.error("Error adding movie '{}': {}", movie.getMovieName(), e.getMessage(), e);
            throw e;
        }
    }

    @PatchMapping("/{moviename}/theatres/{theatre}/tickets")
    public ResponseEntity<Movie> updateTickets(@PathVariable("moviename") String moviename,
                                             @PathVariable("theatre") String theatre,
                                             @RequestParam("total") int total) {
        log.info("Request to update tickets for movie: {} at theatre: {} to total: {}", 
                moviename, theatre, total);
        
        try {
            Movie updatedMovie = movieService.updateTotalTickets(moviename, theatre, total);
            log.info("Tickets updated successfully for movie: {} at theatre: {}, new total: {}", 
                    moviename, theatre, updatedMovie.getTotalTickets());
            return ResponseEntity.ok(updatedMovie);
        } catch (Exception e) {
            log.error("Error updating tickets for movie '{}' at theatre '{}': {}", 
                     moviename, theatre, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{moviename}/delete/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable("moviename") String moviename,
                                          @PathVariable("id") Long id) {
        log.info("Request to delete movie with id: {} (name: {})", id, moviename);
        
        try {
            movieService.deleteMovieById(id);
            log.info("Movie deleted successfully - id: {}, name: {}", id, moviename);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting movie with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{moviename}/delete/{theatre}")
    public ResponseEntity<Void> deleteMovie(@PathVariable("moviename") String moviename,
                                          @PathVariable("theatre") String theatre) {
        log.info("Request to delete movie: {} at theatre: {}", moviename, theatre);
        
        try {
            movieService.deleteMovie(moviename, theatre);
            log.info("Movie deleted successfully - name: {}, theatre: {}", moviename, theatre);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting movie '{}' at theatre '{}': {}", 
                     moviename, theatre, e.getMessage(), e);
            throw e;
        }
    }
} 