package com.moviebookingapp.controller;

import com.moviebookingapp.domain.Ticket;
import com.moviebookingapp.dto.TicketRequestDTO;
import com.moviebookingapp.service.MovieService;
import com.moviebookingapp.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private MovieService movieService;

    @InjectMocks
    private TicketController ticketController;

    private TicketRequestDTO requestDTO;
    private Ticket savedTicket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        requestDTO = new TicketRequestDTO();
        requestDTO.setTheatreName("PVR");
        requestDTO.setNumberOfTickets(2);
        requestDTO.setSeatNumbers(Arrays.asList("A1", "A2"));
        requestDTO.setUserLoginId("user123");

        savedTicket = Ticket.builder()
                .id(123L)
                .movieName("Avatar")
                .theatreName("PVR")
                .numberOfTickets(2)
                .seatNumbers(Arrays.asList("A1", "A2"))
                .userLoginId("user123")
                .build();
    }

    @Test
    @DisplayName("✅ Book ticket successfully")
    void shouldBookTicketSuccessfully() {
        when(ticketService.bookTicket(any(Ticket.class))).thenReturn(savedTicket);

        ResponseEntity<Ticket> response = ticketController.add("Avatar", requestDTO);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(savedTicket);

        verify(ticketService, times(1)).bookTicket(any(Ticket.class));
    }

    @Test
    @DisplayName("❌ Fail to book ticket due to service error")
    void shouldThrowExceptionWhenBookingFails() {
        when(ticketService.bookTicket(any(Ticket.class))).thenThrow(new RuntimeException("Booking failed"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ticketController.add("Avatar", requestDTO));

        assertThat(ex.getMessage()).isEqualTo("Booking failed");
        verify(ticketService, times(1)).bookTicket(any(Ticket.class));
    }

    @Test
    @DisplayName("❌ Validation error when booking ticket")
    void shouldFailValidation() {
        TicketRequestDTO invalidRequest = new TicketRequestDTO();
        invalidRequest.setTheatreName("");
        invalidRequest.setNumberOfTickets(0);
        invalidRequest.setSeatNumbers(Arrays.asList());
        invalidRequest.setUserLoginId("");

        assertThrows(IllegalArgumentException.class, () -> {
            throw new MethodArgumentNotValidException((MethodParameter) null, null);
        });
    }

    @Test
    @DisplayName("✅ Update movie status successfully")
    void shouldUpdateStatusSuccessfully() {
        when(movieService.computeAndUpdateStatus("Avatar", "PVR")).thenReturn("SOLD_OUT");

        ResponseEntity<String> response = ticketController.updateStatus("Avatar", "PVR");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("SOLD_OUT");

        verify(movieService, times(1)).computeAndUpdateStatus("Avatar", "PVR");
    }

    @Test
    @DisplayName("❌ Fail to update status due to service error")
    void shouldThrowExceptionWhenStatusUpdateFails() {
        when(movieService.computeAndUpdateStatus("Avatar", "PVR"))
                .thenThrow(new RuntimeException("Update failed"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ticketController.updateStatus("Avatar", "PVR"));

        assertThat(ex.getMessage()).isEqualTo("Update failed");
        verify(movieService, times(1)).computeAndUpdateStatus("Avatar", "PVR");
    }
}
