package com.moviebookingapp.controller;

import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieControllerTest{

    @Mock
    private MovieService movieService;

    @InjectMocks
    private MovieController movieController;

    private Movie testMovie;

    @BeforeEach
    void setUp() {
        testMovie = Movie.builder()
                .movieName("Test Movie")
                .theatreName("Test Theatre")
                .totalTickets(100)
                .status("BOOK ASAP")
                .build();
    }

    @Test
    void getAllMovies_ReturnsAllMovies() {
        // Arrange
        List<Movie> movies = Arrays.asList(testMovie);
        when(movieService.getAllMovies()).thenReturn(movies);

        // Act
        ResponseEntity<List<Movie>> response = movieController.getAllMovies();

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(movies, response.getBody());
    }

    @Test
    void all_ReturnsEmptyList() {
        // Arrange
        when(movieService.getAllMovies()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<Movie>> response = movieController.getAllMovies();

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void search_ReturnsMatchingMovies() {
        // Arrange
        List<Movie> movies = Arrays.asList(testMovie);
        when(movieService.searchMovies(anyString())).thenReturn(movies);

        // Act
        ResponseEntity<List<Movie>> response = movieController.searchMovies("Test Movie");

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(movies, response.getBody());
        verify(movieService).searchMovies("Test Movie");
    }

    @Test
    void search_ReturnsEmptyWhenNoMatch() {
        // Arrange
        when(movieService.searchMovies(anyString())).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<Movie>> response = movieController.searchMovies("Nonexistent");

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void updateTickets_Success() {
        // Arrange
        Movie updatedMovie = Movie.builder()
                .movieName(testMovie.getMovieName())
                .theatreName(testMovie.getTheatreName())
                .totalTickets(50)
                .status("BOOK ASAP")
                .build();
        when(movieService.updateTotalTickets(anyString(), anyString(), anyInt()))
                .thenReturn(updatedMovie);

        // Act
        ResponseEntity<Movie> response = movieController.updateTickets(
                testMovie.getMovieName(),
                testMovie.getTheatreName(),
                50);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(50, response.getBody().getTotalTickets());
    }

    @Test
    void updateTickets_HandlesException() {
        // Arrange
        when(movieService.updateTotalTickets(anyString(), anyString(), anyInt()))
                .thenThrow(new IllegalArgumentException("Movie not found"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                movieController.updateTickets("Nonexistent", "Nonexistent", 50));
    }

}
