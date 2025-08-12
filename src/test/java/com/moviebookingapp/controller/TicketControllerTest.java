package com.moviebookingapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebookingapp.domain.Ticket;
import com.moviebookingapp.dto.TicketRequestDTO;
import com.moviebookingapp.service.MovieService;
import com.moviebookingapp.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private MovieService movieService;

    @Autowired
    private ObjectMapper objectMapper;

    private Ticket testTicket;
    private TicketRequestDTO ticketRequestDTO;

    @BeforeEach
    void setUp() {
        testTicket = Ticket.builder()
                .id(1L)
                .movieName("Test Movie")
                .theatreName("Test Theatre")
                .numberOfTickets(2)
                .seatNumbers(Arrays.asList("A1", "A2"))
                .userLoginId("testuser")
                .build();

        ticketRequestDTO = new TicketRequestDTO();
        ticketRequestDTO.setTheatreName("Test Theatre");
        ticketRequestDTO.setNumberOfTickets(2);
        ticketRequestDTO.setSeatNumbers(Arrays.asList("A1", "A2"));
        ticketRequestDTO.setUserLoginId("testuser");
    }

    @Test
    void addTicket_Success() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        when(ticketService.bookTicket(any(Ticket.class))).thenReturn(testTicket);

        // Act & Assert
        mockMvc.perform(post("/api/v1.0/moviebooking/{moviename}/add", movieName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticketRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.movieName").value("Test Movie"))
                .andExpect(jsonPath("$.theatreName").value("Test Theatre"))
                .andExpect(jsonPath("$.numberOfTickets").value(2))
                .andExpect(jsonPath("$.userLoginId").value("testuser"));
    }

    @Test
    void addTicket_ValidationFailure() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        ticketRequestDTO.setNumberOfTickets(0); // Invalid: zero tickets

        // Act & Assert
        mockMvc.perform(post("/api/v1.0/moviebooking/{moviename}/add", movieName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticketRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTicket_ServiceException() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        when(ticketService.bookTicket(any(Ticket.class)))
                .thenThrow(new IllegalArgumentException("Not enough tickets available"));

        // Act & Assert
        mockMvc.perform(post("/api/v1.0/moviebooking/{moviename}/add", movieName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticketRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_Success() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        String theatreName = "Test Theatre";
        String expectedStatus = "BOOK ASAP";
        when(movieService.computeAndUpdateStatus(movieName, theatreName)).thenReturn(expectedStatus);

        // Act & Assert
        mockMvc.perform(put("/api/v1.0/moviebooking/{moviename}/update/{ticket}", movieName, theatreName))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedStatus));
    }

    @Test
    void updateStatus_ServiceException() throws Exception {
        // Arrange
        String movieName = "Non Existent Movie";
        String theatreName = "Test Theatre";
        when(movieService.computeAndUpdateStatus(movieName, theatreName))
                .thenThrow(new IllegalArgumentException("Movie not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1.0/moviebooking/{moviename}/update/{ticket}", movieName, theatreName))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTicket_EmptySeatNumbers() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        ticketRequestDTO.setSeatNumbers(Arrays.asList());

        // Act & Assert
        mockMvc.perform(post("/api/v1.0/moviebooking/{moviename}/add", movieName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticketRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTicket_SeatNumbersMismatch() throws Exception {
        // Arrange
        String movieName = "Test Movie";
        ticketRequestDTO.setNumberOfTickets(3);
        ticketRequestDTO.setSeatNumbers(Arrays.asList("A1", "A2")); // Only 2 seats for 3 tickets
        
        when(ticketService.bookTicket(any(Ticket.class)))
                .thenThrow(new IllegalArgumentException("Number of seat numbers must match number of tickets"));

        // Act & Assert
        mockMvc.perform(post("/api/v1.0/moviebooking/{moviename}/add", movieName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticketRequestDTO)))
                .andExpect(status().isBadRequest());
    }
} 