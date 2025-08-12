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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

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
    void getBookedInfo_Success() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        String theatre = "Test Theatre";
        int bookedCount = 50;
        
        when(movieService.bookedCount(movieName, theatre)).thenReturn(bookedCount);
        when(movieService.searchMovies(movieName)).thenReturn(testMovies);

        // Act & Assert
        mockMvc.perform(get("/api/v1.0/moviebooking/{moviename}/booked/{theatre}", movieName, theatre))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booked").value(50))
                .andExpect(jsonPath("$.remaining").value(50))
                .andExpect(jsonPath("$.status").value("BOOK ASAP"));
    }

    @Test
    void getBookedInfo_SoldOut() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        String theatre = "Test Theatre";
        int bookedCount = 100; // All tickets booked
        
        when(movieService.bookedCount(movieName, theatre)).thenReturn(bookedCount);
        when(movieService.searchMovies(movieName)).thenReturn(testMovies);

        // Act & Assert
        mockMvc.perform(get("/api/v1.0/moviebooking/{moviename}/booked/{theatre}", movieName, theatre))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booked").value(100))
                .andExpect(jsonPath("$.remaining").value(0))
                .andExpect(jsonPath("$.status").value("SOLD OUT"));
    }

    @Test
    void getBookedInfo_Overbooked() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        String theatre = "Test Theatre";
        int bookedCount = 120; // More than total tickets (edge case)
        
        when(movieService.bookedCount(movieName, theatre)).thenReturn(bookedCount);
        when(movieService.searchMovies(movieName)).thenReturn(testMovies);

        // Act & Assert
        mockMvc.perform(get("/api/v1.0/moviebooking/{moviename}/booked/{theatre}", movieName, theatre))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booked").value(120))
                .andExpect(jsonPath("$.remaining").value(0)) // Should be clamped to 0
                .andExpect(jsonPath("$.status").value("SOLD OUT"));
    }

    @Test
    void getBookedInfo_MovieNotFound() throws Exception {
        // Arrange
        String movieName = "Non Existent Movie";
        String theatre = "Test Theatre";
        
        when(movieService.bookedCount(movieName, theatre)).thenReturn(0);
        when(movieService.searchMovies(movieName)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1.0/moviebooking/{moviename}/booked/{theatre}", movieName, theatre))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookedInfo_TheatreNotFound() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        String theatre = "Non Existent Theatre";
        
        when(movieService.bookedCount(movieName, theatre)).thenReturn(0);
        when(movieService.searchMovies(movieName)).thenReturn(testMovies);

        // Act & Assert
        mockMvc.perform(get("/api/v1.0/moviebooking/{moviename}/booked/{theatre}", movieName, theatre))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookedInfo_ZeroTickets() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        String theatre = "Test Theatre";
        int bookedCount = 0;
        
        testMovie.setTotalTickets(0);
        when(movieService.bookedCount(movieName, theatre)).thenReturn(bookedCount);
        when(movieService.searchMovies(movieName)).thenReturn(testMovies);

        // Act & Assert
        mockMvc.perform(get("/api/v1.0/moviebooking/{moviename}/booked/{theatre}", movieName, theatre))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booked").value(0))
                .andExpect(jsonPath("$.remaining").value(0))
                .andExpect(jsonPath("$.status").value("SOLD OUT"));
    }

    @Test
    void getBookedInfo_ExactCapacity() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        String theatre = "Test Theatre";
        int bookedCount = 50;
        
        testMovie.setTotalTickets(50); // Exact capacity
        when(movieService.bookedCount(movieName, theatre)).thenReturn(bookedCount);
        when(movieService.searchMovies(movieName)).thenReturn(testMovies);

        // Act & Assert
        mockMvc.perform(get("/api/v1.0/moviebooking/{moviename}/booked/{theatre}", movieName, theatre))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booked").value(50))
                .andExpect(jsonPath("$.remaining").value(0))
                .andExpect(jsonPath("$.status").value("SOLD OUT"));
    }
} 