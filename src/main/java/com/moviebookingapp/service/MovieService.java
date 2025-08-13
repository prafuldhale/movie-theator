package com.moviebookingapp.service;

import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.repository.MovieRepository;
import com.moviebookingapp.repository.TicketRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {
    private final MovieRepository movieRepository;
    private final TicketRepository ticketRepository;

    public List<Movie> getAllMovies() {
        log.debug("Retrieving all movies from database");
        List<Movie> movies = movieRepository.findAll();
        log.info("Retrieved {} movies from database", movies.size());
        return movies;
    }

    public List<Movie> searchMovies(String name) {
        log.debug("Searching movies with name containing: {}", name);
        List<Movie> movies = movieRepository.findByMovieNameContainingIgnoreCase(name);
        log.info("Found {} movies matching search term: {}", movies.size(), name);
        return movies;
    }

    public Movie addMovie(Movie movie) {
        log.debug("Adding new movie: {} at theatre: {}", movie.getMovieName(), movie.getTheatreName());
        log.debug("Movie details - totalTickets: {}, status: {}", movie.getTotalTickets(), movie.getStatus());
        
        Movie savedMovie = movieRepository.save(movie);
        log.info("Movie added successfully - id: {}, name: {}, theatre: {}", 
                savedMovie.getId(), savedMovie.getMovieName(), savedMovie.getTheatreName());
        return savedMovie;
    }

    public void deleteMovieById(Long id) {
        log.debug("Deleting movie with id: {}", id);
        movieRepository.deleteById(id);
        log.info("Movie deleted successfully - id: {}", id);
    }

    public int bookedCount(String movieName, String theatreName) {
        log.debug("Calculating booked count for movie: {} at theatre: {}", movieName, theatreName);
        Long total = ticketRepository.totalBookedForMovieAndTheatre(movieName, theatreName);
        int bookedCount = total == null ? 0 : total.intValue();
        log.debug("Booked count for movie: {} at theatre: {} is {}", movieName, theatreName, bookedCount);
        return bookedCount;
    }

    public String computeAndUpdateStatus(String movieName, String theatreName) {
        log.debug("Computing and updating status for movie: {} at theatre: {}", movieName, theatreName);
        
        Movie movie = movieRepository.findByMovieNameAndTheatreName(movieName, theatreName)
                .orElseThrow(() -> {
                    log.warn("Movie not found for status update - name: {}, theatre: {}", movieName, theatreName);
                    return new IllegalArgumentException("Movie not found");
                });
        
        int booked = bookedCount(movieName, theatreName);
        int remaining = movie.getTotalTickets() - booked;
        String status = remaining <= 0 ? "SOLD OUT" : "BOOK ASAP";
        
        log.debug("Status calculation - totalTickets: {}, booked: {}, remaining: {}, newStatus: {}", 
                movie.getTotalTickets(), booked, remaining, status);
        
        movie.setStatus(status);
        movieRepository.save(movie);
        
        log.info("Status updated for movie: {} at theatre: {} - new status: {}", movieName, theatreName, status);
        return status;
    }

    public Movie updateTotalTickets(String movieName, String theatreName, int total) {
        log.debug("Updating total tickets for movie: {} at theatre: {} to: {}", movieName, theatreName, total);
        
        if (total < 0) {
            log.warn("Invalid total tickets value: {} for movie: {} at theatre: {}", total, movieName, theatreName);
            throw new IllegalArgumentException("Total tickets must be non-negative");
        }
        
        Movie movie = movieRepository.findByMovieNameAndTheatreName(movieName, theatreName)
                .orElseThrow(() -> {
                    log.warn("Movie not found for ticket update - name: {}, theatre: {}", movieName, theatreName);
                    return new IllegalArgumentException("Movie not found");
                });
        
        log.debug("Current movie details - totalTickets: {}, status: {}", movie.getTotalTickets(), movie.getStatus());
        
        movie.setTotalTickets(total);
        int booked = bookedCount(movieName, theatreName);
        int remaining = movie.getTotalTickets() - booked;
        movie.setStatus(remaining <= 0 ? "SOLD OUT" : "BOOK ASAP");
        
        Movie updatedMovie = movieRepository.save(movie);
        log.info("Total tickets updated for movie: {} at theatre: {} - new total: {}, new status: {}", 
                movieName, theatreName, total, updatedMovie.getStatus());
        
        return updatedMovie;
    }

    public void deleteMovie(@NotBlank String movieName, @NotBlank String theatreName) {
        log.debug("Deleting movie by name and theatre - name: {}, theatre: {}", movieName, theatreName);
        
        long deletedCount = movieRepository.deleteByMovieNameAndTheatreName(movieName, theatreName);
        
        if (deletedCount == 0) {
            log.warn("No movie found to delete - name: {}, theatre: {}", movieName, theatreName);
            throw new IllegalArgumentException("Movie not found with name: " + movieName + " and theatre: " + theatreName);
        }
        
        log.info("Movie deleted successfully - name: {}, theatre: {}, deletedCount: {}", 
                movieName, theatreName, deletedCount);
    }
} 