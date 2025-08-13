package com.moviebookingapp.service;

import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.repository.MovieRepository;
import com.moviebookingapp.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest{

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private MovieService movieService;

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
    void getAllMovies_ShouldReturnEmptyList_WhenNoMovies() {
        when(movieRepository.findAll()).thenReturn(Collections.emptyList());

        List<Movie> result = movieService.getAllMovies();
        assertTrue(result.isEmpty());
        verify(movieRepository).findAll();
    }

    @Test
    void getAllMovies_ShouldReturnAllMovies() {
        List<Movie> movies = Arrays.asList(
            testMovie,
            Movie.builder()
                .movieName("Another Movie")
                .theatreName("Another Theatre")
                .totalTickets(50)
                .status("BOOK ASAP")
                .build()
        );

        when(movieRepository.findAll()).thenReturn(movies);

        List<Movie> result = movieService.getAllMovies();
        assertThat(result).hasSize(2);
        assertThat(result).extracting("movieName")
                .containsExactlyInAnyOrder("Test Movie", "Another Movie");
        verify(movieRepository).findAll();
    }

    @Test
    void searchMovies_ShouldReturnMatchingMovies() {
        when(movieRepository.findByMovieNameContainingIgnoreCase(anyString()))
            .thenReturn(Collections.singletonList(testMovie));

        List<Movie> result = movieService.searchMovies("Test Movie");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTheatreName()).isEqualTo("Test Theatre");
        verify(movieRepository).findByMovieNameContainingIgnoreCase("Test Movie");
    }

    @Test
    void addMovie_Success() {
        // Arrange
        when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

        // Act
        Movie result = movieService.addMovie(testMovie);

        // Assert
        assertEquals(testMovie, result);
        verify(movieRepository).save(testMovie);
    }

    @Test
    void deleteMovieById_Success() {
        // Arrange
        Long movieId = 1L;
        doNothing().when(movieRepository).deleteById(movieId);

        // Act
        movieService.deleteMovieById(movieId);

        // Assert
        verify(movieRepository).deleteById(movieId);
    }

    @Test
    void bookedCount_WithBookings() {
        // Arrange
        String movieName = "Test Movie";
        String theatreName = "Test Theatre";
        when(ticketRepository.totalBookedForMovieAndTheatre(movieName, theatreName)).thenReturn(50L);

        // Act
        int result = movieService.bookedCount(movieName, theatreName);

        // Assert
        assertEquals(50, result);
        verify(ticketRepository).totalBookedForMovieAndTheatre(movieName, theatreName);
    }

    @Test
    void bookedCount_NoBookings() {
        // Arrange
        String movieName = "Test Movie";
        String theatreName = "Test Theatre";
        when(ticketRepository.totalBookedForMovieAndTheatre(movieName, theatreName)).thenReturn(null);

        // Act
        int result = movieService.bookedCount(movieName, theatreName);

        // Assert
        assertEquals(0, result);
        verify(ticketRepository).totalBookedForMovieAndTheatre(movieName, theatreName);
    }

    @Test
    void computeAndUpdateStatus_BookAsap() {
        // Arrange
        String movieName = "Test Movie";
        String theatreName = "Test Theatre";
        testMovie.setTotalTickets(100);
        
        when(movieRepository.findByMovieNameAndTheatreName(movieName, theatreName))
                .thenReturn(Optional.of(testMovie));
        when(ticketRepository.totalBookedForMovieAndTheatre(movieName, theatreName)).thenReturn(50L);
        when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

        // Act
        String result = movieService.computeAndUpdateStatus(movieName, theatreName);

        // Assert
        assertEquals("BOOK ASAP", result);
        assertEquals("BOOK ASAP", testMovie.getStatus());
        verify(movieRepository).save(testMovie);
    }

    @Test
    void computeAndUpdateStatus_SoldOut() {
        // Arrange
        String movieName = "Test Movie";
        String theatreName = "Test Theatre";
        testMovie.setTotalTickets(100);
        
        when(movieRepository.findByMovieNameAndTheatreName(movieName, theatreName))
                .thenReturn(Optional.of(testMovie));
        when(ticketRepository.totalBookedForMovieAndTheatre(movieName, theatreName)).thenReturn(100L);
        when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

        // Act
        String result = movieService.computeAndUpdateStatus(movieName, theatreName);

        // Assert
        assertEquals("SOLD OUT", result);
        assertEquals("SOLD OUT", testMovie.getStatus());
        verify(movieRepository).save(testMovie);
    }

    @Test
    void computeAndUpdateStatus_MovieNotFound() {
        // Arrange
        String movieName = "Non Existent Movie";
        String theatreName = "Test Theatre";
        when(movieRepository.findByMovieNameAndTheatreName(movieName, theatreName))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            movieService.computeAndUpdateStatus(movieName, theatreName));
    }

    @Test
    void updateTotalTickets_Success() {
        // Arrange
        String movieName = "Test Movie";
        String theatreName = "Test Theatre";
        int newTotal = 150;
        testMovie.setTotalTickets(100);
        
        when(movieRepository.findByMovieNameAndTheatreName(movieName, theatreName))
                .thenReturn(Optional.of(testMovie));
        when(ticketRepository.totalBookedForMovieAndTheatre(movieName, theatreName)).thenReturn(50L);
        when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

        // Act
        Movie result = movieService.updateTotalTickets(movieName, theatreName, newTotal);

        // Assert
        assertEquals(testMovie, result);
        assertEquals(newTotal, testMovie.getTotalTickets());
        assertEquals("BOOK ASAP", testMovie.getStatus());
        verify(movieRepository).save(testMovie);
    }

    @Test
    void updateTotalTickets_NegativeTotal() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            movieService.updateTotalTickets("Test Movie", "Test Theatre", -10));
    }

    @Test
    void updateTotalTickets_MovieNotFound() {
        // Arrange
        String movieName = "Non Existent Movie";
        String theatreName = "Test Theatre";
        when(movieRepository.findByMovieNameAndTheatreName(movieName, theatreName))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            movieService.updateTotalTickets(movieName, theatreName, 100));
    }

    @Test
    void updateTotalTickets_SoldOut() {
        // Arrange
        String movieName = "Test Movie";
        String theatreName = "Test Theatre";
        int newTotal = 50;
        testMovie.setTotalTickets(100);
        
        when(movieRepository.findByMovieNameAndTheatreName(movieName, theatreName))
                .thenReturn(Optional.of(testMovie));
        when(ticketRepository.totalBookedForMovieAndTheatre(movieName, theatreName)).thenReturn(50L);
        when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

        // Act
        Movie result = movieService.updateTotalTickets(movieName, theatreName, newTotal);

        // Assert
        assertEquals(testMovie, result);
        assertEquals(newTotal, testMovie.getTotalTickets());
        assertEquals("SOLD OUT", testMovie.getStatus());
        verify(movieRepository).save(testMovie);
    }

    @Test
    void deleteMovie_Success() {
        // Arrange
        String movieName = "Test Movie";
        String theatreName = "Test Theatre";
        when(movieRepository.deleteByMovieNameAndTheatreName(movieName, theatreName)).thenReturn(1L);

        // Act
        movieService.deleteMovie(movieName, theatreName);

        // Assert
        verify(movieRepository).deleteByMovieNameAndTheatreName(movieName, theatreName);
    }

    @Test
    void deleteMovie_MovieNotFound() {
        // Arrange
        String movieName = "Non Existent Movie";
        String theatreName = "Test Theatre";
        when(movieRepository.deleteByMovieNameAndTheatreName(movieName, theatreName)).thenReturn(0L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            movieService.deleteMovie(movieName, theatreName));
    }
}
