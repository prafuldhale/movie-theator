package com.moviebookingapp.service;

import com.moviebookingapp.config.TestConfig;
import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@Transactional
class MovieServiceTest {

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    private Movie testMovie;

    @BeforeEach
    void setUp() {
        movieRepository.deleteAll();
        testMovie = Movie.builder()
                .movieName("Test Movie")
                .theatreName("Test Theatre")
                .totalTickets(100)
                .status("BOOK ASAP")
                .build();
    }

    @Test
    void getAllMovies_ShouldReturnEmptyList_WhenNoMovies() {
        List<Movie> result = movieService.getAllMovies();
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllMovies_ShouldReturnAllMovies_WhenMoviesExist() {
        movieRepository.save(testMovie);
        List<Movie> result = movieService.getAllMovies();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMovieName()).isEqualTo("Test Movie");
    }

    @Test
    void searchMovies_ShouldReturnMatchingMovies() {
        movieRepository.save(testMovie);
        List<Movie> result = movieService.searchMovies("Test Movie");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTheatreName()).isEqualTo("Test Theatre");
    }

    @Test
    void searchMovies_ShouldReturnEmptyList_WhenNoMatches() {
        movieRepository.save(testMovie);
        List<Movie> result = movieService.searchMovies("Nonexistent Movie");
        assertTrue(result.isEmpty());
    }

    @Test
    void updateTotalTickets_ShouldUpdateTickets_WhenMovieExists() {
        Movie savedMovie = movieRepository.save(testMovie);
        Movie updatedMovie = movieService.updateTotalTickets(
            savedMovie.getMovieName(),
            savedMovie.getTheatreName(),
            50
        );
        assertThat(updatedMovie.getTotalTickets()).isEqualTo(50);
    }

    @Test
    void updateTotalTickets_ShouldUpdateStatus_WhenZeroTickets() {
        Movie savedMovie = movieRepository.save(testMovie);
        Movie updatedMovie = movieService.updateTotalTickets(
            savedMovie.getMovieName(),
            savedMovie.getTheatreName(),
            0
        );
        assertThat(updatedMovie.getTotalTickets()).isEqualTo(0);
        assertThat(updatedMovie.getStatus()).isEqualTo("SOLD OUT");
    }

    @Test
    void bookedCount_ShouldReturnCorrectCount() {
        Movie savedMovie = movieRepository.save(testMovie);
        int initialTotal = savedMovie.getTotalTickets();
        movieService.updateTotalTickets(
            savedMovie.getMovieName(),
            savedMovie.getTheatreName(),
            initialTotal - 20
        );

        int bookedCount = movieService.bookedCount(
            savedMovie.getMovieName(),
            savedMovie.getTheatreName()
        );
        assertThat(bookedCount).isEqualTo(20);
    }
}
