package com.moviebookingapp.controller;

import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    private Movie testMovie;
    private List<Movie> testMovies;

    @BeforeEach
    void setUp() {
        testMovie = Movie.builder()
                .id(1L)
                .movieName("Test Movie")
                .theatreName("Test Theatre")
                .totalTickets(100)
                .status("BOOK ASAP")
                .build();

        testMovies = Arrays.asList(testMovie);
    }

    @Test
    void getAllMovies_Success() throws Exception {
        // Arrange
        when(movieService.getAllMovies()).thenReturn(testMovies);

        // Act & Assert
        mockMvc.perform(get("/api/v1.0/moviebooking/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].movieName").value("Test Movie"))
                .andExpect(jsonPath("$[0].theatreName").value("Test Theatre"))
                .andExpect(jsonPath("$[0].totalTickets").value(100))
                .andExpect(jsonPath("$[0].status").value("BOOK ASAP"));
    }

    @Test
    void searchMovies_Success() throws Exception {
        // Arrange
        String searchTerm = "Test";
        when(movieService.searchMovies(searchTerm)).thenReturn(testMovies);

        // Act & Assert
        mockMvc.perform(get("/api/v1.0/moviebooking/movies/search/{moviename}", searchTerm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movieName").value("Test Movie"));
    }

    @Test
    void searchMovies_EmptyResult() throws Exception {
        // Arrange
        String searchTerm = "NonExistent";
        when(movieService.searchMovies(searchTerm)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1.0/moviebooking/movies/search/{moviename}", searchTerm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void updateTickets_Success() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        String theatre = "Test Theatre";
        int newTotal = 150;
        when(movieService.updateTotalTickets(movieName, theatre, newTotal)).thenReturn(testMovie);

        // Act & Assert
        mockMvc.perform(patch("/api/v1.0/moviebooking/{moviename}/theatres/{theatre}/tickets", movieName, theatre)
                .param("total", String.valueOf(newTotal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieName").value("Test Movie"));
    }

    @Test
    void updateTickets_ServiceException() throws Exception {
        // Arrange
        String movieName = "Non Existent Movie";
        String theatre = "Test Theatre";
        int newTotal = 150;
        when(movieService.updateTotalTickets(movieName, theatre, newTotal))
                .thenThrow(new IllegalArgumentException("Movie not found"));

        // Act & Assert
        mockMvc.perform(patch("/api/v1.0/moviebooking/{moviename}/theatres/{theatre}/tickets", movieName, theatre)
                .param("total", String.valueOf(newTotal)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMovie_Success() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        Long movieId = 1L;
        doNothing().when(movieService).deleteMovieById(movieId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1.0/moviebooking/{moviename}/delete/{id}", movieName, movieId))
                .andExpect(status().isNoContent());

        verify(movieService).deleteMovieById(movieId);
    }

    @Test
    void deleteMovie_ServiceException() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        Long movieId = 1L;
        doThrow(new IllegalArgumentException("Movie not found")).when(movieService).deleteMovieById(movieId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1.0/moviebooking/{moviename}/delete/{id}", movieName, movieId))
                .andExpect(status().isBadRequest());
    }
} 