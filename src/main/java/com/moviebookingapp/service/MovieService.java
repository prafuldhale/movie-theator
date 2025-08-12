package com.moviebookingapp.service;

import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.repository.MovieRepository;
import com.moviebookingapp.repository.TicketRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final TicketRepository ticketRepository;

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public List<Movie> searchMovies(String name) {
        return movieRepository.findByMovieNameContainingIgnoreCase(name);
    }

    public Movie addMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public void deleteMovieById(Long id) {
        movieRepository.deleteById(id);
    }

    public int bookedCount(String movieName, String theatreName) {
        Long total = ticketRepository.totalBookedForMovieAndTheatre(movieName, theatreName);
        return total == null ? 0 : total.intValue();
    }

    public String computeAndUpdateStatus(String movieName, String theatreName) {
        Movie movie = movieRepository.findByMovieNameAndTheatreName(movieName, theatreName)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));
        int booked = bookedCount(movieName, theatreName);
        int remaining = movie.getTotalTickets() - booked;
        String status = remaining <= 0 ? "SOLD OUT" : "BOOK ASAP";
        movie.setStatus(status);
        movieRepository.save(movie);
        return status;
    }

    public Movie updateTotalTickets(String movieName, String theatreName, int total) {
        if (total < 0) {
            throw new IllegalArgumentException("Total tickets must be non-negative");
        }
        Movie movie = movieRepository.findByMovieNameAndTheatreName(movieName, theatreName)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));
        movie.setTotalTickets(total);
        int booked = bookedCount(movieName, theatreName);
        int remaining = movie.getTotalTickets() - booked;
        movie.setStatus(remaining <= 0 ? "SOLD OUT" : "BOOK ASAP");
        return movieRepository.save(movie);
    }

    public void deleteMovie(@NotBlank String movieName, @NotBlank String theatreName) {
        long deletedCount = movieRepository.deleteByMovieNameAndTheatreName(movieName, theatreName);
        if (deletedCount == 0) {
            throw new IllegalArgumentException("Movie not found with name: " + movieName + " and theatre: " + theatreName);
        }
    }
} 