package com.moviebookingapp.controller;

import com.moviebookingapp.domain.Ticket;
import com.moviebookingapp.service.MovieService;
import com.moviebookingapp.service.TicketService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/moviebooking")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    private final MovieService movieService;

    @PostMapping("/{moviename}/add")
    public ResponseEntity<Ticket> add(@PathVariable("moviename") String moviename,
                                      @Valid @RequestBody TicketRequest request) {
        Ticket ticket = Ticket.builder()
                .movieName(moviename)
                .theatreName(request.getTheatreName())
                .numberOfTickets(request.getNumberOfTickets())
                .seatNumbers(request.getSeatNumbers())
                .userLoginId(request.getUserLoginId())
                .build();
        return ResponseEntity.ok(ticketService.bookTicket(ticket));
    }

    @PutMapping("/{moviename}/update/{ticket}")
    public ResponseEntity<String> updateStatus(@PathVariable("moviename") String moviename,
                                               @PathVariable("ticket") String theatreName) {
        String status = movieService.computeAndUpdateStatus(moviename, theatreName);
        return ResponseEntity.ok(status);
    }

    @Data
    public static class TicketRequest {
        private String theatreName;
        private int numberOfTickets;
        private java.util.List<String> seatNumbers;
        private String userLoginId;
    }
} 