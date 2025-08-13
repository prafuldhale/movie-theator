package com.moviebookingapp.service;

import com.moviebookingapp.config.AppConstants;
import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.domain.Ticket;
import com.moviebookingapp.repository.MovieRepository;
import com.moviebookingapp.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private TicketService ticketService;

    private Ticket ticket;
    private Movie movie;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ticket = Ticket.builder()
                .movieName("Avatar")
                .theatreName("PVR")
                .numberOfTickets(2)
                .seatNumbers(Arrays.asList("A1", "A2"))
                .userLoginId("user123")
                .build();

        movie = new Movie();
        movie.setMovieName("Avatar");
        movie.setTheatreName("PVR");
        movie.setTotalTickets(10);
    }

    @Test
    @DisplayName("✅ Should book ticket successfully")
    void shouldBookTicketSuccessfully() {
        when(movieRepository.findByMovieNameAndTheatreName("Avatar", "PVR")).thenReturn(Optional.of(movie));
        when(ticketRepository.totalBookedForMovieAndTheatre("Avatar", "PVR")).thenReturn(3L);
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        Ticket result = ticketService.bookTicket(ticket);

        assertThat(result).isEqualTo(ticket);
        verify(ticketRepository).save(ticket);
        verify(kafkaTemplate).send(eq(AppConstants.KAFKA_TOPIC_TICKETS), anyString());
    }

    @Test
    @DisplayName("❌ Should fail when numberOfTickets <= 0")
    void shouldFailWhenNumberOfTicketsIsZeroOrNegative() {
        ticket.setNumberOfTickets(0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ticketService.bookTicket(ticket));

        assertThat(ex.getMessage()).isEqualTo("Number of tickets must be positive");
        verifyNoInteractions(movieRepository, ticketRepository, kafkaTemplate);
    }

    @Test
    @DisplayName("❌ Should fail when seatNumbers is null")
    void shouldFailWhenSeatNumbersIsNull() {
        ticket.setSeatNumbers(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ticketService.bookTicket(ticket));

        assertThat(ex.getMessage()).isEqualTo("Seat numbers must be provided");
        verifyNoInteractions(movieRepository, ticketRepository, kafkaTemplate);
    }

    @Test
    @DisplayName("❌ Should fail when seatNumbers is empty")
    void shouldFailWhenSeatNumbersIsEmpty() {
        ticket.setSeatNumbers(Arrays.asList());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ticketService.bookTicket(ticket));

        assertThat(ex.getMessage()).isEqualTo("Seat numbers must be provided");
        verifyNoInteractions(movieRepository, ticketRepository, kafkaTemplate);
    }

    @Test
    @DisplayName("❌ Should fail when seatNumbers count does not match numberOfTickets")
    void shouldFailWhenSeatNumbersCountMismatch() {
        ticket.setSeatNumbers(Arrays.asList("A1"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ticketService.bookTicket(ticket));

        assertThat(ex.getMessage()).isEqualTo("Number of seat numbers must match number of tickets");
        verifyNoInteractions(movieRepository, ticketRepository, kafkaTemplate);
    }

    @Test
    @DisplayName("❌ Should fail when seatNumbers contain duplicates")
    void shouldFailWhenSeatNumbersContainDuplicates() {
        ticket.setSeatNumbers(Arrays.asList("A1", "A1"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ticketService.bookTicket(ticket));

        assertThat(ex.getMessage()).isEqualTo("Duplicate seat numbers are not allowed");
        verifyNoInteractions(movieRepository, ticketRepository, kafkaTemplate);
    }

    @Test
    @DisplayName("❌ Should fail when movie/theatre not found")
    void shouldFailWhenMovieNotFound() {
        when(movieRepository.findByMovieNameAndTheatreName("Avatar", "PVR")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ticketService.bookTicket(ticket));

        assertThat(ex.getMessage()).isEqualTo("Movie/Theatre not found");
        verify(movieRepository).findByMovieNameAndTheatreName("Avatar", "PVR");
        verifyNoInteractions(ticketRepository, kafkaTemplate);
    }

    @Test
    @DisplayName("❌ Should fail when not enough tickets available")
    void shouldFailWhenNotEnoughTicketsAvailable() {
        when(movieRepository.findByMovieNameAndTheatreName("Avatar", "PVR")).thenReturn(Optional.of(movie));
        when(ticketRepository.totalBookedForMovieAndTheatre("Avatar", "PVR")).thenReturn(9L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ticketService.bookTicket(ticket));

        assertThat(ex.getMessage()).isEqualTo("Not enough tickets available");
        verify(ticketRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    @DisplayName("❌ Should propagate exception from repository save")
    void shouldPropagateExceptionFromSave() {
        when(movieRepository.findByMovieNameAndTheatreName("Avatar", "PVR")).thenReturn(Optional.of(movie));
        when(ticketRepository.totalBookedForMovieAndTheatre("Avatar", "PVR")).thenReturn(0L);
        when(ticketRepository.save(ticket)).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ticketService.bookTicket(ticket));

        assertThat(ex.getMessage()).isEqualTo("DB error");
        verify(kafkaTemplate, never()).send(any(), any());
    }
}
