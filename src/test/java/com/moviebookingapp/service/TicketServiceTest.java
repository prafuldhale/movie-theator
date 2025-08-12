package com.moviebookingapp.service;

import com.moviebookingapp.config.AppConstants;
import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.domain.Ticket;
import com.moviebookingapp.repository.MovieRepository;
import com.moviebookingapp.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private TicketService ticketService;

    private Ticket testTicket;
    private Movie testMovie;

    @BeforeEach
    void setUp() {
        testMovie = Movie.builder()
                .id(1L)
                .movieName("Test Movie")
                .theatreName("Test Theatre")
                .totalTickets(100)
                .status("BOOK ASAP")
                .build();

        testTicket = Ticket.builder()
                .id(1L)
                .movieName("Test Movie")
                .theatreName("Test Theatre")
                .numberOfTickets(2)
                .seatNumbers(Arrays.asList("A1", "A2"))
                .build();
    }

    @Test
    void bookTicket_Success() {
        // Arrange
        when(movieRepository.findByMovieNameAndTheatreName("Test Movie", "Test Theatre"))
                .thenReturn(Optional.of(testMovie));
        when(ticketRepository.totalBookedForMovieAndTheatre("Test Movie", "Test Theatre"))
                .thenReturn(50L);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(null);

        // Act
        Ticket result = ticketService.bookTicket(testTicket);

        // Assert
        assertNotNull(result);
        verify(ticketRepository).save(testTicket);
        verify(kafkaTemplate).send(AppConstants.KAFKA_TOPIC_TICKETS, 
                "Test Movie|Test Theatre|2");
    }

    @Test
    void bookTicket_MovieNotFound() {
        // Arrange
        when(movieRepository.findByMovieNameAndTheatreName("Test Movie", "Test Theatre"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            ticketService.bookTicket(testTicket));
    }

    @Test
    void bookTicket_NotEnoughTicketsAvailable() {
        // Arrange
        when(movieRepository.findByMovieNameAndTheatreName("Test Movie", "Test Theatre"))
                .thenReturn(Optional.of(testMovie));
        when(ticketRepository.totalBookedForMovieAndTheatre("Test Movie", "Test Theatre"))
                .thenReturn(99L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            ticketService.bookTicket(testTicket));
    }

    @Test
    void bookTicket_NoPreviousBookings() {
        // Arrange
        when(movieRepository.findByMovieNameAndTheatreName("Test Movie", "Test Theatre"))
                .thenReturn(Optional.of(testMovie));
        when(ticketRepository.totalBookedForMovieAndTheatre("Test Movie", "Test Theatre"))
                .thenReturn(null);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(null);

        // Act
        Ticket result = ticketService.bookTicket(testTicket);

        // Assert
        assertNotNull(result);
        verify(ticketRepository).save(testTicket);
    }

    @Test
    void bookTicket_ZeroTickets() {
        // Arrange
        testTicket.setNumberOfTickets(0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            ticketService.bookTicket(testTicket));
    }

    @Test
    void bookTicket_NegativeTickets() {
        // Arrange
        testTicket.setNumberOfTickets(-1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            ticketService.bookTicket(testTicket));
    }

    @Test
    void bookTicket_NullSeatNumbers() {
        // Arrange
        testTicket.setSeatNumbers(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            ticketService.bookTicket(testTicket));
    }

    @Test
    void bookTicket_EmptySeatNumbers() {
        // Arrange
        testTicket.setSeatNumbers(Arrays.asList());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            ticketService.bookTicket(testTicket));
    }

    @Test
    void bookTicket_SeatNumbersMismatch() {
        // Arrange
        testTicket.setNumberOfTickets(3);
        testTicket.setSeatNumbers(Arrays.asList("A1", "A2")); // Only 2 seats for 3 tickets

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            ticketService.bookTicket(testTicket));
    }

    @Test
    void bookTicket_DuplicateSeatNumbers() {
        // Arrange
        testTicket.setSeatNumbers(Arrays.asList("A1", "A1")); // Duplicate seats

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            ticketService.bookTicket(testTicket));
    }

    @Test
    void bookTicket_ExactCapacity() {
        // Arrange
        testMovie.setTotalTickets(50);
        when(movieRepository.findByMovieNameAndTheatreName("Test Movie", "Test Theatre"))
                .thenReturn(Optional.of(testMovie));
        when(ticketRepository.totalBookedForMovieAndTheatre("Test Movie", "Test Theatre"))
                .thenReturn(48L); // 48 + 2 = 50 (exact capacity)
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(null);

        // Act
        Ticket result = ticketService.bookTicket(testTicket);

        // Assert
        assertNotNull(result);
        verify(ticketRepository).save(testTicket);
    }

    @Test
    void bookTicket_OverCapacity() {
        // Arrange
        testMovie.setTotalTickets(50);
        when(movieRepository.findByMovieNameAndTheatreName("Test Movie", "Test Theatre"))
                .thenReturn(Optional.of(testMovie));
        when(ticketRepository.totalBookedForMovieAndTheatre("Test Movie", "Test Theatre"))
                .thenReturn(49L); // 49 + 2 = 51 (over capacity)

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            ticketService.bookTicket(testTicket));
    }
} 