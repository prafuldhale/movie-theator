package com.moviebookingapp.controller;

import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.dto.BookedInfoDTO;
import com.moviebookingapp.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    @Mock
    private MovieService movieService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Movie createMovie(String movieName, String theatreName, int totalTickets) {
        Movie movie = new Movie();
        movie.setMovieName(movieName);
        movie.setTheatreName(theatreName);
        movie.setTotalTickets(totalTickets);
        return movie;
    }

    @Test
    void testBooked_BookAsap() {
        when(movieService.bookedCount("Inception", "PVR")).thenReturn(5);
        when(movieService.searchMovies("Inception")).thenReturn(List.of(createMovie("Inception", "PVR", 10)));

        ResponseEntity<BookedInfoDTO> response = adminController.booked("Inception", "PVR");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(5, response.getBody().getBooked());
        assertEquals(5, response.getBody().getRemaining());
        assertEquals("BOOK ASAP", response.getBody().getStatus());
    }

    @Test
    void testBooked_SoldOut_ExactMatch() {
        when(movieService.bookedCount("Inception", "PVR")).thenReturn(10);
        when(movieService.searchMovies("Inception")).thenReturn(List.of(createMovie("Inception", "PVR", 10)));

        ResponseEntity<BookedInfoDTO> response = adminController.booked("Inception", "PVR");

        assertEquals("SOLD OUT", response.getBody().getStatus());
        assertEquals(0, response.getBody().getRemaining());
    }

    @Test
    void testBooked_SoldOut_Overbooked() {
        when(movieService.bookedCount("Inception", "PVR")).thenReturn(15);
        when(movieService.searchMovies("Inception")).thenReturn(List.of(createMovie("Inception", "PVR", 10)));

        ResponseEntity<BookedInfoDTO> response = adminController.booked("Inception", "PVR");

        assertEquals(0, response.getBody().getRemaining()); // Safeguard
        assertEquals("SOLD OUT", response.getBody().getStatus());
    }

    @Test
    void testBooked_CaseInsensitiveTheatreMatch() {
        when(movieService.bookedCount("Inception", "pvr")).thenReturn(3);
        when(movieService.searchMovies("Inception")).thenReturn(List.of(createMovie("Inception", "PVR", 5)));

        ResponseEntity<BookedInfoDTO> response = adminController.booked("Inception", "pvr");

        assertEquals("BOOK ASAP", response.getBody().getStatus());
    }

    @Test
    void testBooked_NoMovieFound() {
        when(movieService.bookedCount("Inception", "PVR")).thenReturn(2);
        when(movieService.searchMovies("Inception")).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> adminController.booked("Inception", "PVR"));
    }

    @Test
    void testBooked_TheatreNotMatching() {
        when(movieService.bookedCount("Inception", "PVR")).thenReturn(2);
        when(movieService.searchMovies("Inception")).thenReturn(List.of(createMovie("Inception", "IMAX", 5)));

        assertThrows(IllegalArgumentException.class,
                () -> adminController.booked("Inception", "PVR"));
    }

    @Test
    void testBooked_BookedCountThrowsException() {
        when(movieService.bookedCount(anyString(), anyString()))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> adminController.booked("Inception", "PVR"));
    }

    @Test
    void testBooked_SearchMoviesThrowsException() {
        when(movieService.bookedCount("Inception", "PVR")).thenReturn(2);
        when(movieService.searchMovies(anyString()))
                .thenThrow(new RuntimeException("Service down"));

        assertThrows(RuntimeException.class,
                () -> adminController.booked("Inception", "PVR"));
    }
}
